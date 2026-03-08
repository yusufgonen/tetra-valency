package com.td.game.utils;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.td.game.map.GameMap;

public class Dreamlo {

    private static final String PRIVATE_WAVE = "j0EmJJRzs06lST-oLkyRlg67rnUSDnkEuMHI2gq4nhTg";
    private static final String PUBLIC_WAVE = "69ad975a8f40bc1a1452c01c";

    private static final String PRIVATE_TIME = "mj34txQ4wE27Fg1TXo64fQya3DGwHVUkimVizDoY_VRw";
    private static final String PUBLIC_TIME = "69ad988f8f40bc1a1452c110";

    private static final String BASE_URL = "http://dreamlo.com/lb/";
    private static final float DUPLICATE_RETRY_DELAY_SECONDS = 1.2f;
    private static final long FETCH_CACHE_WINDOW_MS = 1500L;
    private static final int MAX_FETCH_RETRY = 1;
    private static final Object FETCH_LOCK = new Object();

    private static final class PendingFetch {
        final GameMap.MapType mapType;
        final LeaderboardCallback callback;

        PendingFetch(GameMap.MapType mapType, LeaderboardCallback callback) {
            this.mapType = mapType;
            this.callback = callback;
        }
    }

    private static boolean timeFetchInFlight;
    private static boolean waveFetchInFlight;
    private static long timeCacheAtMs;
    private static long waveCacheAtMs;
    private static String[][] timeCacheData = new String[0][0];
    private static String[][] waveCacheData = new String[0][0];
    private static final List<PendingFetch> pendingTimeFetches = new ArrayList<>();
    private static final List<PendingFetch> pendingWaveFetches = new ArrayList<>();

    public interface LeaderboardCallback {
        void onLoaded(String[][] data);
    }

    public static void uploadWaveScore(String name, int wave) {
        uploadWaveScore(name, wave, GameMap.MapType.ELEMENTAL_CASTLE);
    }

    public static void uploadWaveScore(String name, int wave, GameMap.MapType mapType) {
        String url = BASE_URL + PRIVATE_WAVE + "/add/" + sanitize(withMapTag(name, mapType)) + "/" + wave;
        sendRequest(url);
    }

    public static void uploadTimeScore(String name, float totalSeconds) {
        uploadTimeScore(name, totalSeconds, GameMap.MapType.ELEMENTAL_CASTLE);
    }

    public static void uploadTimeScore(String name, float totalSeconds, GameMap.MapType mapType) {

        String url = BASE_URL + PRIVATE_TIME + "/add/" + sanitize(withMapTag(name, mapType)) + "/1/"
                + (int) totalSeconds;
        sendRequest(url);
    }

    public static void fetchScores(final boolean isTimeBoard, final LeaderboardCallback callback) {
        fetchScores(isTimeBoard, null, callback);
    }

    public static void fetchScores(final boolean isTimeBoard, final GameMap.MapType mapType,
            final LeaderboardCallback callback) {
        if (callback == null) {
            return;
        }

        String[][] cached = null;
        boolean shouldStartFetch = false;
        long now = System.currentTimeMillis();
        synchronized (FETCH_LOCK) {
            if (isTimeBoard) {
                if (now - timeCacheAtMs <= FETCH_CACHE_WINDOW_MS) {
                    cached = filterRows(timeCacheData, mapType, true);
                } else if (timeFetchInFlight) {
                    pendingTimeFetches.add(new PendingFetch(mapType, callback));
                } else {
                    timeFetchInFlight = true;
                    pendingTimeFetches.add(new PendingFetch(mapType, callback));
                    shouldStartFetch = true;
                }
            } else {
                if (now - waveCacheAtMs <= FETCH_CACHE_WINDOW_MS) {
                    cached = filterRows(waveCacheData, mapType, false);
                } else if (waveFetchInFlight) {
                    pendingWaveFetches.add(new PendingFetch(mapType, callback));
                } else {
                    waveFetchInFlight = true;
                    pendingWaveFetches.add(new PendingFetch(mapType, callback));
                    shouldStartFetch = true;
                }
            }
        }

        if (cached != null) {
            callback.onLoaded(cached);
            return;
        }
        if (shouldStartFetch) {
            requestScoresWithRetry(isTimeBoard, 0);
        }
    }

    private static void requestScoresWithRetry(boolean isTimeBoard, int retryCount) {
        String code = isTimeBoard ? PUBLIC_TIME : PUBLIC_WAVE;
        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        request.setUrl(BASE_URL + code + "/pipe/50");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String body = httpResponse.getResultAsString();
                if (isDuplicateRequestError(body) && retryCount < MAX_FETCH_RETRY) {
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            requestScoresWithRetry(isTimeBoard, retryCount + 1);
                        }
                    }, DUPLICATE_RETRY_DELAY_SECONDS);
                    return;
                }
                finishFetch(isTimeBoard, parseRows(body));
            }

            @Override
            public void failed(Throwable t) {
                if (retryCount < MAX_FETCH_RETRY) {
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            requestScoresWithRetry(isTimeBoard, retryCount + 1);
                        }
                    }, DUPLICATE_RETRY_DELAY_SECONDS);
                    return;
                }
                finishFetch(isTimeBoard, new String[0][0]);
            }

            @Override
            public void cancelled() {
                finishFetch(isTimeBoard, new String[0][0]);
            }
        });
    }

    private static void finishFetch(boolean isTimeBoard, String[][] rows) {
        List<PendingFetch> pending;
        synchronized (FETCH_LOCK) {
            if (isTimeBoard) {
                timeCacheData = rows;
                timeCacheAtMs = System.currentTimeMillis();
                timeFetchInFlight = false;
                pending = new ArrayList<>(pendingTimeFetches);
                pendingTimeFetches.clear();
            } else {
                waveCacheData = rows;
                waveCacheAtMs = System.currentTimeMillis();
                waveFetchInFlight = false;
                pending = new ArrayList<>(pendingWaveFetches);
                pendingWaveFetches.clear();
            }
        }

        for (PendingFetch p : pending) {
            p.callback.onLoaded(filterRows(rows, p.mapType, isTimeBoard));
        }
    }

    private static String[][] parseRows(String result) {
        if (result == null || result.trim().isEmpty()) {
            return new String[0][0];
        }
        String[] lines = result.split("\\n");
        Array<String[]> rows = new Array<>();
        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length < 3) {
                continue;
            }
            rows.add(new String[] { stripMapTag(parts[0]), parts[1], parts[2], mapPrefix(readMapType(parts[0])) });
        }
        String[][] data = new String[rows.size][4];
        for (int i = 0; i < rows.size; i++) {
            data[i] = rows.get(i);
        }
        return data;
    }

    private static String[][] filterRows(String[][] data, GameMap.MapType mapType, boolean isTimeBoard) {
        if (data == null || data.length == 0) {
            return new String[0][0];
        }
        ArrayList<String[]> rows = new ArrayList<>();
        for (String[] row : data) {
            if (row.length < 4) {
                continue;
            }
            GameMap.MapType rowMap = "DO_".equals(row[3]) ? GameMap.MapType.DESERT_OASIS
                    : GameMap.MapType.ELEMENTAL_CASTLE;
            if (mapType != null && rowMap != mapType) {
                continue;
            }
            rows.add(new String[] { row[0], isTimeBoard ? row[2] : row[1] });
        }

        if (isTimeBoard) {
            // azalan
            rows.sort((a, b) -> Float.compare(parseFloatSafe(a[1]), parseFloatSafe(b[1])));
        } else {
            // artan
            rows.sort((a, b) -> Integer.compare(parseIntSafe(b[1]), parseIntSafe(a[1])));
        }

        int count = Math.min(5, rows.size());
        String[][] out = new String[count][2];
        for (int i = 0; i < count; i++) {
            out[i] = rows.get(i);
        }
        return out;
    }

    private static int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private static float parseFloatSafe(String value) {
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            return Float.MAX_VALUE;
        }
    }

    private static boolean isDuplicateRequestError(String body) {
        if (body == null) {
            return false;
        }
        String msg = body.toLowerCase();
        return msg.contains("same request within") && msg.contains("second");
    }

    private static void sendRequest(String url) {
        sendRequest(url, 0);
    }

    private static void sendRequest(String url, int retryCount) {
        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        request.setUrl(url);
        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String body = httpResponse.getResultAsString();
                if (retryCount >= 1 || body == null) {
                    return;
                }
                String msg = body.toLowerCase();
                if (msg.contains("same request within") && msg.contains("second")) {
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            sendRequest(url, retryCount + 1);
                        }
                    }, DUPLICATE_RETRY_DELAY_SECONDS);
                }
            }

            @Override
            public void failed(Throwable t) {
            }

            @Override
            public void cancelled() {
            }
        });
    }

    private static String sanitize(String name) {
        try {
            return java.net.URLEncoder.encode(name, "UTF-8").replace("+", "%20");
        } catch (Exception e) {
            return name.replace(" ", "%20");
        }
    }

    private static String withMapTag(String name, GameMap.MapType mapType) {
        String safeName = name == null || name.trim().isEmpty() ? "Player" : name.trim();
        return mapPrefix(mapType) + safeName;
    }

    private static String mapPrefix(GameMap.MapType mapType) {
        return mapType == GameMap.MapType.DESERT_OASIS ? "DO_" : "EC_";
    }

    private static GameMap.MapType readMapType(String taggedName) {
        if (taggedName != null && taggedName.startsWith("DO_")) {
            return GameMap.MapType.DESERT_OASIS;
        }
        return GameMap.MapType.ELEMENTAL_CASTLE;
    }

    private static String stripMapTag(String taggedName) {
        if (taggedName == null || taggedName.isEmpty()) {
            return "Player";
        }
        if (taggedName.startsWith("EC_") || taggedName.startsWith("DO_")) {
            return taggedName.substring(3);
        }
        return taggedName;
    }
}
