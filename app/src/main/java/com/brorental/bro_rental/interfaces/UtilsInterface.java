package com.brorental.bro_rental.interfaces;

import androidx.appcompat.app.AlertDialog;

import com.brorental.bro_rental.models.RideHistoryModel;

import java.util.HashMap;

public interface UtilsInterface {

    interface RefreshInterface {

        void refresh(int catePosition);
    }

    interface SearchInterface {

        void refresh(String query);
    }

    interface RideInterface {
        void refresh(HashMap<String, Object> map);
    }


    interface RideHistoryListener {
        void updateStatus(String status, String docId, int pos, RideHistoryModel data);
        void contactListener(String type);
    }

    interface NoKycInterface {
        void refresh(AlertDialog alertDialog);
    }
}


