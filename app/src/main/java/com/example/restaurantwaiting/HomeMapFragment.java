package com.example.restaurantwaiting;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class HomeMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private View detailCard;
    private TextView tvResName, tvExpectedTime, tvCongestionText, tvWaitingTeams, tvRating;
    private View btnShareStatus;
    private com.google.android.material.button.MaterialButton btnReservation;
    private RestaurantData.Info currentInfo;
    private Marker myLocationMarker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_map, container, false);

        detailCard = view.findViewById(R.id.restaurant_detail_card);
        tvResName = view.findViewById(R.id.restaurant_name);
        tvExpectedTime = view.findViewById(R.id.tv_expected_time);
        tvCongestionText = view.findViewById(R.id.tv_congestion_text);
        tvWaitingTeams = view.findViewById(R.id.tv_waiting_teams);
        tvRating = view.findViewById(R.id.restaurant_rating);
        btnShareStatus = view.findViewById(R.id.btn_share_status);
        btnReservation = view.findViewById(R.id.btn_reservation);

        btnReservation.setOnClickListener(v -> {
            if (btnReservation.getText().toString().equals("바로 입장")) {
                showEntryConfirmDialog();
            } else {
                showReservationDialog();
            }
        });

        btnShareStatus.setOnClickListener(v -> showShareStatusDialog());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment_container);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().add(R.id.map_fragment_container, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        RestaurantData.checkDataFreshness();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }

        // 초기 카메라 위치 설정
        LatLng myLocation = new LatLng(35.14573, 129.0072);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 17.5f));

        addSampleMarkers();

        // 데이터 변경 리스너 등록
        RestaurantData.setListener(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    addSampleMarkers();
                    if (currentInfo != null) {
                        RestaurantData.Info updated = RestaurantData.getByName(currentInfo.name);
                        if (updated != null) {
                            updateDetailCardFromData(updated);
                        }
                    }
                });
            }
        });

        googleMap.setOnMarkerClickListener(marker -> {
            RestaurantData.Info info = (RestaurantData.Info) marker.getTag();
            if (info != null) {
                currentInfo = info;
                updateDetailCardFromData(info);
                detailCard.setVisibility(View.VISIBLE);
            }
            return false;
        });

        googleMap.setOnMapClickListener(latLng -> detailCard.setVisibility(View.GONE));
    }

    private void updateDetailCardFromData(RestaurantData.Info info) {
        tvResName.setText(info.name);
        tvRating.setText(info.rating);
        tvExpectedTime.setText(info.expectedTime);
        tvCongestionText.setText(info.congestion);
        tvCongestionText.setTextColor(info.color);
        // 숫자 대신 status 필드를 사용하여 제보 식당은 "-"가 나오도록 수정
        tvWaitingTeams.setText(info.status);
        tvWaitingTeams.setTextColor(info.color);

        // 제보 기반 식당(키오스크 미제공)과 키오스크 식당의 버튼 노출 분리
        if (!info.isKioskAvailable) {
            // 제보 식당: 상황 공유 버튼만 노출
            btnShareStatus.setVisibility(View.VISIBLE);
            btnReservation.setVisibility(View.GONE);
        } else {
            // 키오스크 식당: 예약 관련 버튼만 노출 (상황 공유 숨김)
            btnShareStatus.setVisibility(View.GONE);
            btnReservation.setVisibility(View.VISIBLE);

            // 버튼 상태 제어
            if (info.isClosed) {
                btnReservation.setText("입장 마감");
                btnReservation.setEnabled(false);
                btnReservation.setAlpha(0.5f);
            } else if (info.status.equals("즉시 입장 가능")) {
                btnReservation.setText("바로 입장");
                btnReservation.setEnabled(true);
                btnReservation.setAlpha(1.0f);
            } else {
                btnReservation.setText("예약하기");
                btnReservation.setEnabled(true);
                btnReservation.setAlpha(1.0f);
            }
        }
    }

    private void addSampleMarkers() {
        googleMap.clear();
        
        // 내 위치 마커 추가 (하늘색 blue.png)
        LatLng myLocation = new LatLng(35.14573, 129.0072);
        googleMap.addMarker(new MarkerOptions()
                .position(myLocation)
                .title("내 위치")
                .anchor(0.5f, 1.0f) // 하늘색 마커도 다른 마커와 동일하게 하단 앵커 적용
                .icon(getScaledMarker(R.drawable.blue, true)));

        // 식당 마커 추가
        for (RestaurantData.Info info : RestaurantData.getRestaurants()) {
            LatLng pos = getLatLngForRestaurant(info.name);
            MarkerOptions options = new MarkerOptions()
                    .position(pos)
                    .title(info.name)
                    .anchor(0.5f, 1.0f); // 핀 마커는 하단 중앙 기준
            
            Marker marker = googleMap.addMarker(options);
            if (marker != null) {
                marker.setTag(info);
                updateMarkerIcon(marker, info);
            }
        }
    }

    private LatLng getLatLngForRestaurant(String name) {
        switch (name) {
            case "용빈각": return new LatLng(35.146572, 129.008249);
            case "경대컵밥": return new LatLng(35.146784, 129.008264);
            case "부르다": return new LatLng(35.146757, 129.008509);
            case "닭다구리": return new LatLng(35.146780, 129.007798);
            case "투가이즈피자&치킨": return new LatLng(35.146797, 129.009561);
            case "꽃돼지국밥": return new LatLng(35.146754, 129.008419);
            case "꼬망": return new LatLng(35.146596, 129.008481);
            case "엄마밥상": return new LatLng(35.146923, 129.008642);
            default: return new LatLng(35.14573, 129.0072);
        }
    }

    private void updateMarkerIcon(Marker marker, RestaurantData.Info info) {
        int resId;
        if (info.isClosed) {
            resId = R.drawable.black;
        } else if (info.status.equals("즉시 입장 가능")) {
            resId = R.drawable.dark_green;
        } else if (info.status.equals("알 수 없음")) {
            resId = R.drawable.purple;
        } else {
            if (info.waitingTeams >= 1 && info.waitingTeams <= 3) {
                resId = R.drawable.lime;
            } else if (info.waitingTeams >= 4 && info.waitingTeams <= 10) {
                resId = R.drawable.yellow;
            } else if (info.waitingTeams > 10) {
                resId = R.drawable.orange;
            } else {
                resId = R.drawable.purple;
            }
        }
        marker.setIcon(getScaledMarker(resId, false));
    }

    // 마커 아이콘 크기를 조절하는 헬퍼 메서드 (모든 마커 크기 통일)
    private com.google.android.gms.maps.model.BitmapDescriptor getScaledMarker(int resId, boolean isMyLocation) {
        float density = getResources().getDisplayMetrics().density;
        int width, height;

        // 하늘색 마커를 포함한 모든 마커를 동일한 핀 크기로 조정 (너비 35dp, 높이 55dp)
        width = (int) (35 * density);
        height = (int) (55 * density);

        BitmapDrawable bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(requireContext(), resId);
        if (bitmapDrawable == null) return BitmapDescriptorFactory.fromResource(resId);
        
        Bitmap bitmap = bitmapDrawable.getBitmap();
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap);
    }

    private void showShareStatusDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_share_status, null);
        Spinner spStatus = dialogView.findViewById(R.id.spinner_status);
        Spinner spCongestion = dialogView.findViewById(R.id.spinner_congestion);

        String[] statuses = {"대기 중", "입장 마감"};
        String[] congestions = {"대기열 없음", "한산", "보통", "혼잡함"};

        spStatus.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, statuses));
        spCongestion.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, congestions));

        // 입장 마감 선택 시 혼잡도 선택 비활성화
        spStatus.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (statuses[position].equals("입장 마감")) {
                    spCongestion.setEnabled(false);
                    spCongestion.setAlpha(0.5f);
                } else {
                    spCongestion.setEnabled(true);
                    spCongestion.setAlpha(1.0f);
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        new AlertDialog.Builder(requireContext())
                .setTitle("실시간 현장 정보 공유")
                .setView(dialogView)
                .setPositiveButton("공유", (d, w) -> {
                    String status = spStatus.getSelectedItem().toString();
                    String congestion = spCongestion.getSelectedItem().toString();

                    RestaurantData.Info info = RestaurantData.getByName(tvResName.getText().toString());
                    if (info != null) {
                        // 상황 공유 시 선택한 혼잡도에 따라 가상의 대기 팀 수를 배정하여 마커 색상에 반영
                        int teams = 0;
                        if (!status.equals("입장 마감")) {
                            switch (congestion) {
                                case "대기열 없음": teams = 0; break;
                                case "한산": teams = 2; break;
                                case "보통": teams = 7; break;
                                case "혼잡함": teams = 15; break;
                            }
                        }
                        info.update(status, congestion, teams);
                        RestaurantData.notifyDataChanged();
                    }
                    Toast.makeText(getContext(), "제보 감사합니다!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", (d, w) -> {
                    Toast.makeText(getContext(), "소중한 의견 감사합니다.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showEntryConfirmDialog() {
        if (currentInfo == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("바로 입장")
                .setMessage("식당에 즉시 입장하시겠습니까?")
                .setPositiveButton("확인", (d, w) -> {
                    ReservationManager.getInstance().setRestaurantName(currentInfo.name);
                    ReservationManager.getInstance().setWaitingTeams(0);
                    ReservationManager.getInstance().setCurrentState(ReservationManager.State.DINING);
                    
                    // RestaurantData에도 상태 반영 (선택 사항이지만 일관성을 위해)
                    currentInfo.update("식사 중", "보통", 0);
                    RestaurantData.notifyDataChanged();

                    Toast.makeText(getContext(), currentInfo.name + " 식사를 시작합니다.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void showReservationDialog() {
        if (currentInfo == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("예약하기")
                .setMessage(currentInfo.name + " 예약을 진행하시겠습니까?")
                .setPositiveButton("확인", (d, w) -> {
                    ReservationManager.getInstance().setRestaurantName(currentInfo.name);
                    ReservationManager.getInstance().setWaitingTeams(currentInfo.waitingTeams);
                    ReservationManager.getInstance().setCurrentState(ReservationManager.State.RESERVED);
                    Toast.makeText(getContext(), currentInfo.name + " 예약이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null)
                .show();
    }
}
