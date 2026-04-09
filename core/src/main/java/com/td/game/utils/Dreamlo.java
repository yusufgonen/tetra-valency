package com.td.game.utils;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Timer;
import com.td.game.map.GameMap;
import com.td.game.systems.OptionsData;

public class Dreamlo {

    private static final String PRIVATE_WAVE = "j0EmJJRzs06lST-oLkyRlg67rnUSDnkEuMHI2gq4nhTg";
    private static final String PUBLIC_WAVE = "69ad975a8f40bc1a1452c01c";

    private static final String PRIVATE_TIME = "mj34txQ4wE27Fg1TXo64fQya3DGwHVUkimVizDoY_VRw";
    private static final String PUBLIC_TIME = "69ad988f8f40bc1a1452c110";

    private static final String BASE_URL = "http://dreamlo.com/lb/";
    private static final float DUPLICATE_RETRY_DELAY_SECONDS = 1.2f;
    private static final float RENAME_READD_DELAY_SECONDS = 0.35f;
    private static final long FETCH_CACHE_WINDOW_MS = 1500L;
    private static final int MAX_FETCH_RETRY = 1;
    private static final Object FETCH_LOCK = new Object();

    private static final class PendingFetch {
        final GameMap.MapType mapType;
        final int limit;
        final LeaderboardCallback callback;

        PendingFetch(GameMap.MapType mapType, int limit, LeaderboardCallback callback) {
            this.mapType = mapType;
            this.limit = limit;
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

    public interface PersonalBestCallback {
        void onLoaded(PersonalBest best);
    }

    public static final class PersonalBest {
        public final int highestWave;
        public final float bestTimeSeconds;
        public final boolean hasWave;
        public final boolean hasTime;

        public PersonalBest(int highestWave, float bestTimeSeconds, boolean hasWave, boolean hasTime) {
            this.highestWave = highestWave;
            this.bestTimeSeconds = bestTimeSeconds;
            this.hasWave = hasWave;
            this.hasTime = hasTime;
        }
    }

    private interface RawScoresCallback {
        void onLoaded(String[][] data);
    }

    public static void fetchPersonalBest(String playerName, GameMap.MapType mapType, PersonalBestCallback callback) {
        if (callback == null) {
            return;
        }
        String safeName = playerName == null ? "" : playerName.trim();
        if (safeName.isEmpty()) {
            callback.onLoaded(new PersonalBest(0, 0f, false, false));
            return;
        }

        final String normalized = safeName.toLowerCase();
        final GameMap.MapType safeMap = mapType == null ? GameMap.MapType.ELEMENTAL_CASTLE : mapType;
        final String[][][] waveRowsHolder = new String[1][][];
        final String[][][] timeRowsHolder = new String[1][][];

        RawScoresCallback maybeFinish = rows -> {
            if (waveRowsHolder[0] == null || timeRowsHolder[0] == null) {
                return;
            }

            int bestWave = Integer.MIN_VALUE;
            float bestTime = Float.MAX_VALUE;
            boolean hasWave = false;
            boolean hasTime = false;

            for (String[] row : waveRowsHolder[0]) {
                if (!matchesPlayerAndMap(row, normalized, safeMap)) {
                    continue;
                }
                int wave = parseIntSafe(row[1]);
                if (!hasWave || wave > bestWave) {
                    bestWave = wave;
                    hasWave = true;
                }
            }

            for (String[] row : timeRowsHolder[0]) {
                if (!matchesPlayerAndMap(row, normalized, safeMap)) {
                    continue;
                }
                float seconds = parseFloatSafe(row[2]);
                if (seconds <= 0f || seconds == Float.MAX_VALUE) {
                    continue;
                }
                if (!hasTime || seconds < bestTime) {
                    bestTime = seconds;
                    hasTime = true;
                }
            }

            callback.onLoaded(new PersonalBest(
                    hasWave ? bestWave : 0,
                    hasTime ? bestTime : 0f,
                    hasWave,
                    hasTime));
        };

        fetchRawScores(false, rows -> {
            waveRowsHolder[0] = rows == null ? new String[0][0] : rows;
            maybeFinish.onLoaded(rows);
        });
        fetchRawScores(true, rows -> {
            timeRowsHolder[0] = rows == null ? new String[0][0] : rows;
            maybeFinish.onLoaded(rows);
        });
    }

    public static boolean isBetterWaveRecord(int currentWave, PersonalBest best) {
        return best == null || !best.hasWave || currentWave > best.highestWave;
    }

    public static boolean isBetterTimeRecord(float currentSeconds, PersonalBest best) {
        if (currentSeconds <= 0f || currentSeconds == Float.MAX_VALUE) {
            return false;
        }
        return best == null || !best.hasTime || currentSeconds < best.bestTimeSeconds;
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

    public static void renamePlayer(String oldName, String newName) {
        if (oldName == null || newName == null) {
            return;
        }
        String trimmedOld = oldName.trim();
        String trimmedNew = newName.trim();
        if (trimmedOld.isEmpty() || trimmedNew.isEmpty() || trimmedOld.equalsIgnoreCase(trimmedNew)) {
            return;
        }

        final String[][][] timeRowsHolder = new String[1][][];
        final String[][][] waveRowsHolder = new String[1][][];

        RawScoresCallback maybeApplyRename = rows -> {
            if (timeRowsHolder[0] == null || waveRowsHolder[0] == null) {
                return;
            }
            renameInRows(timeRowsHolder[0], trimmedOld, trimmedNew, true);
            renameInRows(waveRowsHolder[0], trimmedOld, trimmedNew, false);
            invalidateFetchCache();
        };

        fetchRawScores(true, rows -> {
            timeRowsHolder[0] = rows == null ? new String[0][0] : rows;
            maybeApplyRename.onLoaded(rows);
        });
        fetchRawScores(false, rows -> {
            waveRowsHolder[0] = rows == null ? new String[0][0] : rows;
            maybeApplyRename.onLoaded(rows);
        });
    }

    public static void renamePlayerUsingLocalBest(String oldName, String newName, OptionsData options) {
        if (oldName == null || newName == null || options == null) {
            return;
        }
        String trimmedOld = oldName.trim();
        String trimmedNew = newName.trim();
        if (trimmedOld.isEmpty() || trimmedNew.isEmpty() || trimmedOld.equalsIgnoreCase(trimmedNew)) {
            return;
        }

        for (GameMap.MapType mapType : GameMap.MapType.values()) {
            String oldTagged = withMapTag(trimmedOld, mapType);
            // Fire delete more than once to tolerate transient Dreamlo throttling/network hiccups.
            deleteScore(PRIVATE_TIME, oldTagged);
            deleteScore(PRIVATE_WAVE, oldTagged);
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    deleteScore(PRIVATE_TIME, oldTagged);
                    deleteScore(PRIVATE_WAVE, oldTagged);
                }
            }, 0.55f);
        }

        int mapOrdinal = 0;
        for (GameMap.MapType mapType : GameMap.MapType.values()) {
            String key = mapType.name();
            int wave = options.highestWaveByMap == null ? 0 : options.highestWaveByMap.get(key, 0);
            float time = options.bestTimeByMap == null ? Float.MAX_VALUE : options.bestTimeByMap.get(key, Float.MAX_VALUE);
            float baseDelay = 1.0f + (mapOrdinal * 0.25f);

            if (wave > 0) {
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        uploadWaveScore(trimmedNew, wave, mapType);
                    }
                }, baseDelay);
            }

            if (time > 0f && time != Float.MAX_VALUE) {
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        uploadTimeScore(trimmedNew, time, mapType);
                    }
                }, baseDelay + 0.12f);
            }

            mapOrdinal++;
        }

        invalidateFetchCache();
    }

    public static void fetchScores(final boolean isTimeBoard, final LeaderboardCallback callback) {
        fetchScores(isTimeBoard, null, 5, callback);
    }

    public static void fetchScores(final boolean isTimeBoard, final GameMap.MapType mapType,
            final LeaderboardCallback callback) {
        fetchScores(isTimeBoard, mapType, 5, callback);
    }

    public static void fetchScores(final boolean isTimeBoard, final GameMap.MapType mapType, final int limit,
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
                    cached = filterRows(timeCacheData, mapType, true, limit);
                } else if (timeFetchInFlight) {
                    pendingTimeFetches.add(new PendingFetch(mapType, limit, callback));
                } else {
                    timeFetchInFlight = true;
                    pendingTimeFetches.add(new PendingFetch(mapType, limit, callback));
                    shouldStartFetch = true;
                }
            } else {
                if (now - waveCacheAtMs <= FETCH_CACHE_WINDOW_MS) {
                    cached = filterRows(waveCacheData, mapType, false, limit);
                } else if (waveFetchInFlight) {
                    pendingWaveFetches.add(new PendingFetch(mapType, limit, callback));
                } else {
                    waveFetchInFlight = true;
                    pendingWaveFetches.add(new PendingFetch(mapType, limit, callback));
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
            p.callback.onLoaded(filterRows(rows, p.mapType, isTimeBoard, p.limit));
        }
    }

    private static void fetchRawScores(boolean isTimeBoard, RawScoresCallback callback) {
        if (callback == null) {
            return;
        }
        String code = isTimeBoard ? PUBLIC_TIME : PUBLIC_WAVE;
        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        request.setUrl(BASE_URL + code + "/pipe/50");
        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                callback.onLoaded(parseRows(httpResponse.getResultAsString()));
            }

            @Override
            public void failed(Throwable t) {
                callback.onLoaded(new String[0][0]);
            }

            @Override
            public void cancelled() {
                callback.onLoaded(new String[0][0]);
            }
        });
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

    private static boolean matchesPlayerAndMap(String[] row, String normalizedPlayerName, GameMap.MapType mapType) {
        if (row == null || row.length < 4 || normalizedPlayerName == null || normalizedPlayerName.isEmpty()) {
            return false;
        }
        String rowName = row[0] == null ? "" : row[0].trim().toLowerCase();
        if (!normalizedPlayerName.equals(rowName)) {
            return false;
        }
        GameMap.MapType rowMap = mapTypeFromPrefix(row[3]);
        return rowMap == mapType;
    }

    private static String[][] filterRows(String[][] data, GameMap.MapType mapType, boolean isTimeBoard, int limit) {
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
            
            rows.sort((a, b) -> Float.compare(parseFloatSafe(a[1]), parseFloatSafe(b[1])));
        } else {
            
            rows.sort((a, b) -> Integer.compare(parseIntSafe(b[1]), parseIntSafe(a[1])));
        }

        int safeLimit = limit <= 0 ? 5 : limit;
        int count = Math.min(safeLimit, rows.size());
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

    private static void renameInRows(String[][] rows, String oldName, String newName, boolean isTimeBoard) {
        if (rows == null || rows.length == 0) {
            return;
        }

        if (isTimeBoard) {
            ObjectMap<GameMap.MapType, Float> bestByMap = new ObjectMap<>();
            for (String[] row : rows) {
                if (row.length < 4 || !oldName.equalsIgnoreCase(row[0])) {
                    continue;
                }
                GameMap.MapType mapType = mapTypeFromPrefix(row[3]);
                float seconds = parseFloatSafe(row[2]);
                if (seconds <= 0f || seconds == Float.MAX_VALUE) {
                    continue;
                }
                float currentBest = bestByMap.get(mapType, Float.MAX_VALUE);
                if (seconds < currentBest) {
                    bestByMap.put(mapType, seconds);
                }
            }

            int mapOrdinal = 0;
            for (GameMap.MapType mapType : GameMap.MapType.values()) {
                if (!bestByMap.containsKey(mapType)) {
                    mapOrdinal++;
                    continue;
                }
                float seconds = bestByMap.get(mapType, Float.MAX_VALUE);
                if (seconds <= 0f || seconds == Float.MAX_VALUE) {
                    mapOrdinal++;
                    continue;
                }
                deleteScore(PRIVATE_TIME, withMapTag(oldName, mapType));
                float delay = RENAME_READD_DELAY_SECONDS + (0.2f * mapOrdinal);
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        uploadTimeScore(newName, seconds, mapType);
                    }
                }, delay);
                mapOrdinal++;
            }
            return;
        }

        ObjectMap<GameMap.MapType, Integer> bestByMap = new ObjectMap<>();
        for (String[] row : rows) {
            if (row.length < 4 || !oldName.equalsIgnoreCase(row[0])) {
                continue;
            }
            GameMap.MapType mapType = mapTypeFromPrefix(row[3]);
            int wave = parseIntSafe(row[1]);
            if (wave <= 0) {
                continue;
            }
            int currentBest = bestByMap.get(mapType, 0);
            if (wave > currentBest) {
                bestByMap.put(mapType, wave);
            }
        }

        int mapOrdinal = 0;
        for (GameMap.MapType mapType : GameMap.MapType.values()) {
            if (!bestByMap.containsKey(mapType)) {
                mapOrdinal++;
                continue;
            }
            int wave = bestByMap.get(mapType, 0);
            if (wave <= 0) {
                mapOrdinal++;
                continue;
            }
            deleteScore(PRIVATE_WAVE, withMapTag(oldName, mapType));
            float delay = RENAME_READD_DELAY_SECONDS + (0.2f * mapOrdinal);
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    uploadWaveScore(newName, wave, mapType);
                }
            }, delay);
            mapOrdinal++;
        }
    }

    private static void invalidateFetchCache() {
        synchronized (FETCH_LOCK) {
            timeCacheAtMs = 0L;
            waveCacheAtMs = 0L;
            timeCacheData = new String[0][0];
            waveCacheData = new String[0][0];
        }
    }

    private static void deleteScore(String privateKey, String taggedName) {
        if (privateKey == null || taggedName == null) {
            return;
        }
        String url = BASE_URL + privateKey + "/delete/" + sanitize(taggedName);
        sendRequest(url);
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

    private static GameMap.MapType mapTypeFromPrefix(String prefix) {
        if ("DO_".equals(prefix)) {
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
