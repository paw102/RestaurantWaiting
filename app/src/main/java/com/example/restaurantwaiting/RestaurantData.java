package com.example.restaurantwaiting;

import android.graphics.Color;
import java.util.ArrayList;
import java.util.List;

public class RestaurantData {
    public static class Info {
        public String name;
        public String status;
        public String expectedTime;
        public String congestion;
        public int color;
        public boolean isClosed;
        public boolean isKioskAvailable;
        public int waitingTeams;
        public String rating;
        public long lastUpdated;

        public Info(String name, String status, String expectedTime, String colorHex, String congestion, boolean isClosed, boolean isKioskAvailable, int waitingTeams, String rating) {
            this.name = name;
            this.status = status;
            this.expectedTime = expectedTime;
            this.color = Color.parseColor(colorHex);
            this.congestion = congestion;
            this.isClosed = isClosed;
            this.isKioskAvailable = isKioskAvailable;
            this.waitingTeams = waitingTeams;
            this.rating = rating;
            this.lastUpdated = System.currentTimeMillis();
        }

        public void update(String status, String congestion, int ignoredTeams) {
            this.congestion = congestion;
            this.lastUpdated = System.currentTimeMillis();

            if (status.equals("입장 마감")) {
                this.status = "입장 마감";
                this.congestion = "입장 마감";
                this.color = Color.BLACK;
                this.isClosed = true;
                this.expectedTime = "-";
            } else if (status.equals("알 수 없음")) {
                this.status = "알 수 없음";
                this.congestion = "알 수 없음";
                this.color = Color.parseColor("#9C27B0");
                this.isClosed = false;
                this.expectedTime = "-";
            } else {
                this.isClosed = false;
                
                // 마커 색상은 선택한 혼잡도 문구에 따라 결정
                switch (congestion) {
                    case "대기열 없음":
                        this.color = Color.parseColor("#2E7D32");
                        break;
                    case "한산":
                        this.color = Color.parseColor("#8BC34A");
                        break;
                    case "보통":
                        this.color = Color.parseColor("#FBC02D");
                        break;
                    case "혼잡함":
                        this.color = Color.parseColor("#FF9800");
                        break;
                }

                if (!isKioskAvailable) {
                    this.status = "-";
                    this.expectedTime = "-";
                }
            }
        }
    }

    private static List<Info> restaurants = new ArrayList<>();

    static {
        // 초기 데이터: 예상 대기 시간은 팀당 7분으로 표시, 혼잡도는 "-"로 시작, 별점 차별화
        restaurants.add(new Info("용빈각", "대기 3팀", "약 21분", "#8BC34A", "-", false, true, 3, "★ 4.2 (128)"));
        restaurants.add(new Info("경대컵밥", "즉시 입장 가능", "약 0분", "#2E7D32", "-", false, true, 0, "★ 4.5 (326)"));
        restaurants.add(new Info("부르다", "대기 2팀", "약 14분", "#8BC34A", "-", false, true, 2, "★ 4.8 (512)"));
        restaurants.add(new Info("닭다구리", "대기 4팀", "약 28분", "#FBC02D", "-", false, true, 4, "★ 3.7 (85)"));
        restaurants.add(new Info("투가이즈피자&치킨", "대기 12팀", "약 84분", "#FF9800", "-", false, true, 12, "★ 4.4 (210)"));
        restaurants.add(new Info("꽃돼지국밥", "입장 마감", "-", "#000000", "입장 마감", true, true, 0, "★ 4.6 (430)"));
        restaurants.add(new Info("꼬망", "-", "-", "#9C27B0", "-", false, false, 0, "★ 4.1 (62)"));
        restaurants.add(new Info("엄마밥상", "-", "-", "#9C27B0", "-", false, false, 0, "★ 4.7 (243)"));
    }

    public static List<Info> getRestaurants() {
        return restaurants;
    }

    public static Info getByName(String name) {
        for (Info info : restaurants) {
            if (info.name.equals(name)) return info;
        }
        return null;
    }

    public interface OnDataChangedListener {
        void onDataChanged();
    }

    private static OnDataChangedListener listener;
    public static void setListener(OnDataChangedListener l) { listener = l; }
    public static void notifyDataChanged() { if (listener != null) listener.onDataChanged(); }

    public static void checkDataFreshness() {
        long currentTime = System.currentTimeMillis();
        boolean changed = false;
        for (Info info : restaurants) {
            if (!info.isKioskAvailable && (currentTime - info.lastUpdated > 3600000)) {
                if (!info.status.equals("알 수 없음")) {
                    info.status = "알 수 없음";
                    info.congestion = "-";
                    info.color = Color.parseColor("#9C27B0");
                    info.waitingTeams = 0;
                    info.expectedTime = "-";
                    changed = true;
                }
            }
        }
        if (changed) notifyDataChanged();
    }
}
