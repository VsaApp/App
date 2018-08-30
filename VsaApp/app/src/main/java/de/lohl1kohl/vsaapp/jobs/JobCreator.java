package de.lohl1kohl.vsaapp.jobs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;

public class JobCreator implements com.evernote.android.job.JobCreator {

    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case StartJob.TAG:
                return new StartJob();
            case EndJob.TAG:
                return new EndJob();
            default:
                return null;
        }
    }
}
