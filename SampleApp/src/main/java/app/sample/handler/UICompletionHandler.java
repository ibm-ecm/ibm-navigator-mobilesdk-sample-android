/*
 * Licensed Materials - Property of IBM
 * (C) Copyright IBM Corporation 2015, 2023. All Rights Reserved.
 * This sample program is provided AS IS and may be used, executed, copied
 * and modified without royalty payment by customer (a) for its own instruction
 * and study, (b) in order to develop applications designed to run with an IBM
 * product, either for customer's own internal use or for redistribution by
 * customer, as part of such an application, in customer's own products.
 */
package app.sample.handler;

import android.os.Handler;
import android.os.Looper;

import com.ibm.ecm.api.coresdk.exception.IBMECMRuntimeException;
import com.ibm.ecm.api.coresdk.model.IBMECMCompletionHandler;

/**
 Important: This sample project is not intended to be a tutorial on how to design or create an Android application.
 It is only for the purpose of showing how to use the IBM Content Navigator SDK.
 For example, it does not contain robust error handling, recovery, good structure, or design.

 The IBMECMCompletionHandler is run in the background. If a UI update is needed, this UICompletionHandler is called which calls the completion
 in the main UI thread.
 */
public abstract class UICompletionHandler<T> extends IBMECMCompletionHandler<T> {

    private Handler handler = new Handler(Looper.getMainLooper());

    public abstract void onCompletedUI(final T result);

    public abstract void onErrorUI(final IBMECMRuntimeException exp);

    public void onCompleted(final T result) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onCompletedUI(result);
            }
        });
    }

    public void onError(final IBMECMRuntimeException exp) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onErrorUI(exp);
            }
        });
    }

}
