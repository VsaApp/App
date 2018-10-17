package de.lohl1kohl.vsaapp.jobs;

import android.content.Context;
import android.support.annotation.NonNull;
import com.evernote.android.job.JobCreator;
import com.evernote.android.job.JobManager;

public final class Receiver extends JobCreator.AddJobCreatorReceiver {
    @Override
    protected void addJobCreator(@NonNull Context context, @NonNull JobManager manager) {
        manager.addJobCreator(new de.lohl1kohl.vsaapp.jobs.JobCreator());
    }
}
