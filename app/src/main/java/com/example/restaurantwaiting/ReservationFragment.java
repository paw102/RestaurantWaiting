package com.example.restaurantwaiting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class ReservationFragment extends Fragment {

    private TextView tvResName, tvWaitingCount;
    private View btnEnter, cardInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservation, container, false);

        tvResName = view.findViewById(R.id.tv_res_name);
        tvWaitingCount = view.findViewById(R.id.tv_waiting_count);
        btnEnter = view.findViewById(R.id.btn_enter);
        cardInfo = view.findViewById(R.id.card_reservation_info);

        updateUI();

        btnEnter.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("입장 확인")
                    .setMessage("입장하시겠습니까?")
                    .setPositiveButton("예", (dialog, which) -> {
                        ReservationManager.getInstance().setCurrentState(ReservationManager.State.DINING);
                        Toast.makeText(getContext(), "식사가 시작되었습니다. MY 페이지로 이동합니다.", Toast.LENGTH_SHORT).show();
                        updateUI();
                    })
                    .setNegativeButton("아니오", null)
                    .show();
        });

        return view;
    }

    private void updateUI() {
        ReservationManager rm = ReservationManager.getInstance();
        if (rm.getCurrentState() == ReservationManager.State.RESERVED) {
            tvResName.setText(rm.getRestaurantName());
            tvWaitingCount.setText(String.valueOf(rm.getWaitingTeams()) + "팀");
            btnEnter.setVisibility(View.VISIBLE);
            cardInfo.setVisibility(View.VISIBLE);
        } else if (rm.getCurrentState() == ReservationManager.State.DINING) {
            tvResName.setText(rm.getRestaurantName() + " (식사 중)");
            tvWaitingCount.setText("0팀");
            btnEnter.setVisibility(View.GONE);
            cardInfo.setVisibility(View.VISIBLE);
        } else {
            tvResName.setText("예약 내역이 없습니다.");
            tvWaitingCount.setText("-");
            btnEnter.setVisibility(View.GONE);
            cardInfo.setVisibility(View.VISIBLE);
        }
    }
}
