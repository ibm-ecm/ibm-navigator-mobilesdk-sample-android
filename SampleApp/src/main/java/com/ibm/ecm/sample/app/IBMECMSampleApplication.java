package com.ibm.ecm.sample.app;

import android.app.Application;

import com.ibm.ecm.api.coresdk.model.IBMECMApplication;
import com.ibm.ecm.api.coresdk.model.IBMECMDesktop;
import com.ibm.ecm.api.coresdk.model.IBMECMRepository;

/**
 Important: This sample project is not intended to be a tutorial on how to design or create an Android application.
 It is only for the purpose of showing how to use the IBM Content Navigator SDK.
 For example, it does not contain robust error handling, recovery, good structure, or design.
 */
public class IBMECMSampleApplication extends Application {

    private IBMECMApplication application;
    private IBMECMDesktop desktop;
    private IBMECMRepository repository;

    public IBMECMApplication getApplication() {
        return application;
    }

    public void setApplication(IBMECMApplication application) {
        this.application = application;
    }

    public IBMECMDesktop getDesktop() {
        return desktop;
    }

    public void setDesktop(IBMECMDesktop desktop) {
        this.desktop = desktop;
    }

    public IBMECMRepository getRepository() {
        return repository;
    }

    public void setRepository(IBMECMRepository repository) {
        this.repository = repository;
    }
}
