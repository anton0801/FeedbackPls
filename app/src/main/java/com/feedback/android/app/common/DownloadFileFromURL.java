package com.feedback.android.app.common;


import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadFileFromURL extends AsyncTask<String, String, File> {

    private TaskOnFinishedListener listener;
    private String destinationPath;

    public DownloadFileFromURL(TaskOnFinishedListener listener, String destinationPath) {
        this.listener = listener;
        this.destinationPath = destinationPath;
    }

    /**
     * Downloading file in background thread
     */
    @Override
    protected File doInBackground(String... f_url) {
        int count;
        try {
            Log.d("LOG_D", "doInBackground: " + f_url[0]);
            Log.d("LOG_D", "doInBackground: " + destinationPath);
            URL url = new URL(f_url[0]);
            URLConnection connection = url.openConnection();
            connection.connect();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);

            // Output stream
            OutputStream output = new FileOutputStream(destinationPath);

            byte data[] = new byte[1024];

            while ((count = input.read(data)) != -1) {
                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

            return new File(destinationPath);
        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }

        return null;
    }

    /**
     * After completing background task Dismiss the progress dialog
     **/
    @Override
    protected void onPostExecute(File file) {
        // dismiss the dialog after the file was downloaded
        listener.taskFinished(file);
    }

    public interface TaskOnFinishedListener {
        void taskFinished(File file);
    }

}
