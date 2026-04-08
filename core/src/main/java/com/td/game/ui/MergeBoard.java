package com.td.game.ui;

import com.td.game.elements.Element;

public class MergeBoard {
    private float x, y, width, height;
    private float slotSize;
    private Element slot1, slot2, resultSlot;

    public MergeBoard(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    public void setSlotSize(float s) {
        this.slotSize = s;
    }

    public boolean isInBounds(float screenX, float screenY) {
        return screenX >= x && screenX <= x + width && screenY >= y && screenY <= y + height;
    }

    public boolean hasSlot1() {
        return slot1 != null;
    }

    public boolean hasSlot2() {
        return slot2 != null;
    }

    public boolean hasResult() {
        return resultSlot != null;
    }

    public boolean placeOrb(float tapX, float tapY, Element e) {
        if (tapX >= getSlot1X() && tapX <= getSlot1X() + slotSize && tapY >= getSlot1Y()
                && tapY <= getSlot1Y() + slotSize) {
            if (slot1 == null) {
                slot1 = e;
                checkMerge();
                return true;
            }
        } else if (tapX >= getSlot2X() && tapX <= getSlot2X() + slotSize && tapY >= getSlot2Y()
                && tapY <= getSlot2Y() + slotSize) {
            if (slot2 == null) {
                slot2 = e;
                checkMerge();
                return true;
            }
        }
        return false;
    }

    private void checkMerge() {
        if (resultSlot != null) {
            return;
        }
        if (slot1 != null && slot2 != null) {
            Element result = Element.merge(slot1, slot2);
            if (result != null) {
                resultSlot = result;
                slot1 = null;
                slot2 = null;
            } else {
                
                
                
            }
        }
    }

    public Element tryTakeResult(float tapX, float tapY) {
        if (resultSlot != null && tapX >= getResultX() && tapX <= getResultX() + slotSize && tapY >= getResultY()
                && tapY <= getResultY() + slotSize) {
            Element res = resultSlot;
            resultSlot = null;
            return res;
        }
        return null;
    }

    public void tryResolveMerge() {
        checkMerge();
    }

    public Element takeResult() {
        if (resultSlot == null) {
            return null;
        }
        Element res = resultSlot;
        resultSlot = null;
        return res;
    }

    public Element tryTakeInputOrb(float tapX, float tapY) {
        if (slot1 != null && tapX >= getSlot1X() && tapX <= getSlot1X() + slotSize && tapY >= getSlot1Y()
                && tapY <= getSlot1Y() + slotSize) {
            Element res = slot1;
            slot1 = null;
            return res;
        }
        if (slot2 != null && tapX >= getSlot2X() && tapX <= getSlot2X() + slotSize && tapY >= getSlot2Y()
                && tapY <= getSlot2Y() + slotSize) {
            Element res = slot2;
            slot2 = null;
            return res;
        }
        return null;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getHeight() {
        return slotSize;
    }

    public float getSlotSize() {
        return slotSize;
    }

    public float getSlot1X() {
        return x;
    }

    public float getSlot1Y() {
        return y;
    }

    public float getSlot2X() {
        return x + slotSize + 25f;
    }

    public float getSlot2Y() {
        return y;
    }

    public float getResultX() {
        return x + (slotSize + 25f) * 2;
    }

    public float getResultY() {
        return y;
    }

    public Element getResultElement() {
        return resultSlot;
    }

    public Element getSlot1Element() {
        return slot1;
    }

    public Element getSlot2Element() {
        return slot2;
    }

    public void setSlot1Element(Element e) {
        slot1 = e;
    }

    public void setSlot2Element(Element e) {
        slot2 = e;
    }

    public void setResultElement(Element e) {
        resultSlot = e;
    }
}

