package com.example.restaurantwaiting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class MyPageFragment extends Fragment {

    private TextView tvMyResName, tvDiningStatus;
    private View btnFinishMeal, btnEnterRestaurant;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_page, container, false);

        tvMyResName = view.findViewById(R.id.tv_my_res_name);
        tvDiningStatus = view.findViewById(R.id.tv_dining_status);
        btnFinishMeal = view.findViewById(R.id.btn_finish_meal);
        btnEnterRestaurant = view.findViewById(R.id.btn_enter_restaurant);

        updateUI();

        btnFinishMeal.setOnClickListener(v -> showFinishMealDialog());
        btnEnterRestaurant.setOnClickListener(v -> {
            ReservationManager.getInstance().setCurrentState(ReservationManager.State.DINING);
            updateUI();
            Toast.makeText(getContext(), "식사를 시작합니다!", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        ReservationManager rm = ReservationManager.getInstance();
        if (rm.getCurrentState() == ReservationManager.State.DINING) {
            tvMyResName.setText(rm.getRestaurantName());
            tvDiningStatus.setText("식사 중...");
            tvDiningStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            tvDiningStatus.setVisibility(View.VISIBLE);
            btnFinishMeal.setVisibility(View.VISIBLE);
            btnEnterRestaurant.setVisibility(View.GONE);
        } else if (rm.getCurrentState() == ReservationManager.State.RESERVED) {
            tvMyResName.setText(rm.getRestaurantName());
            tvDiningStatus.setText("예약 완료 (입장 대기 중)");
            tvDiningStatus.setTextColor(android.graphics.Color.parseColor("#F44336"));
            tvDiningStatus.setVisibility(View.VISIBLE);
            btnFinishMeal.setVisibility(View.GONE);
            btnEnterRestaurant.setVisibility(View.VISIBLE);
        } else {
            tvMyResName.setText("현재 이용 중인 식당 없음");
            tvDiningStatus.setVisibility(View.GONE);
            btnFinishMeal.setVisibility(View.GONE);
            btnEnterRestaurant.setVisibility(View.GONE);
        }
    }

    private void showFinishMealDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("식사 종료")
                .setMessage("식사를 마치셨습니까?")
                .setPositiveButton("예", (dialog, which) -> {
                    showCongestionDialog();
                })
                .setNegativeButton("아니오", null)
                .show();
    }

    private void showCongestionDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_finish_meal, null);
        Spinner spStatus = dialogView.findViewById(R.id.spinner_status_finish);
        Spinner spCongestion = dialogView.findViewById(R.id.spinner_congestion_finish);

        String[] statuses = {"대기 중", "입장 마감"};
        String[] congestions = {"대기열 없음", "한산", "보통", "혼잡함"};

        spStatus.setAdapter(new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, statuses));
        spCongestion.setAdapter(new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, congestions));

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
                .setTitle("식사는 어떠셨나요?\n현재 식당의 상태를 알려주세요.")
                .setView(dialogView)
                .setPositiveButton("완료", (d, w) -> {
                    String status = spStatus.getSelectedItem().toString();
                    String congestion = spCongestion.getSelectedItem().toString();

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

                    String resName = ReservationManager.getInstance().getRestaurantName();
                    RestaurantData.Info info = RestaurantData.getByName(resName);
                    if (info != null) {
                        info.update(status, congestion, teams);
                        RestaurantData.notifyDataChanged();
                    }

                    Toast.makeText(getContext(), "소중한 정보 감사합니다!", Toast.LENGTH_SHORT).show();
                    ReservationManager.getInstance().reset();
                    updateUI();
                })
                .setNegativeButton("취소", (d, w) -> {
                    Toast.makeText(getContext(), "소중한 의견 감사합니다.", Toast.LENGTH_SHORT).show();
                    ReservationManager.getInstance().reset();
                    updateUI();
                })
                .setCancelable(false)
                .show();
    }
}
