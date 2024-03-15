package com.adamcalculator.dynamicpack;

import java.util.OptionalLong;

public interface DownloadListener {
    void onStart();

    void onContentLength(OptionalLong contentLength);

    void onProgress(long total);

    void onFinish(boolean b);
}
