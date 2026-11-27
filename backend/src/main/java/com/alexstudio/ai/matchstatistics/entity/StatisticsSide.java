package com.alexstudio.ai.matchstatistics.entity;
import java.math.BigDecimal;
import jakarta.persistence.Embeddable;
@Embeddable
public class StatisticsSide {
    private Integer bigChances;
    private Integer clearances;
    private Integer corners;
    private Integer crosses;
    private Integer interceptions;
    private Integer keyPasses;
    private Integer longPasses;
    private Integer passAccuracy;
    private Integer passesFinalThird;
    private Integer possession;
    private Integer redCards;
    private Integer shots;
    private Integer shotsOnTarget;
    private Integer touchesInBox;
    private BigDecimal xg;
    private Integer yellowCards;
    public Integer getBigChances() {
        return bigChances;
    }
    public void setBigChances(Integer value) {
        bigChances = value;
    }
    public Integer getClearances() {
        return clearances;
    }
    public void setClearances(Integer value) {
        clearances = value;
    }
    public Integer getCorners() {
        return corners;
    }
    public void setCorners(Integer value) {
        corners = value;
    }
    public Integer getCrosses() {
        return crosses;
    }
    public void setCrosses(Integer value) {
        crosses = value;
    }
    public Integer getInterceptions() {
        return interceptions;
    }
    public void setInterceptions(Integer value) {
        interceptions = value;
    }
    public Integer getKeyPasses() {
        return keyPasses;
    }
    public void setKeyPasses(Integer value) {
        keyPasses = value;
    }
    public Integer getLongPasses() {
        return longPasses;
    }
    public void setLongPasses(Integer value) {
        longPasses = value;
    }
    public Integer getPassAccuracy() {
        return passAccuracy;
    }
    public void setPassAccuracy(Integer value) {
        passAccuracy = value;
    }
    public Integer getPassesFinalThird() {
        return passesFinalThird;
    }
    public void setPassesFinalThird(Integer value) {
        passesFinalThird = value;
    }
    public Integer getPossession() {
        return possession;
    }
    public void setPossession(Integer value) {
        possession = value;
    }
    public Integer getRedCards() {
        return redCards;
    }
    public void setRedCards(Integer value) {
        redCards = value;
    }
    public Integer getShots() {
        return shots;
    }
    public void setShots(Integer value) {
        shots = value;
    }
    public Integer getShotsOnTarget() {
        return shotsOnTarget;
    }
    public void setShotsOnTarget(Integer value) {
        shotsOnTarget = value;
    }
    public Integer getTouchesInBox() {
        return touchesInBox;
    }
    public void setTouchesInBox(Integer value) {
        touchesInBox = value;
    }
    public BigDecimal getXg() {
        return xg;
    }
    public void setXg(BigDecimal value) {
        xg = value;
    }
    public Integer getYellowCards() {
        return yellowCards;
    }
    public void setYellowCards(Integer value) {
        yellowCards = value;
    }
}
