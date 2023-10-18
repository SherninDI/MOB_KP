package com.example.kp;

public interface DeviceListInteractionListener<T> {
    void startLoading();
    void endLoading(boolean partialResults);
    void onItemClick(T item);
    void endLoadingWithDialog(boolean error, T element);
}
