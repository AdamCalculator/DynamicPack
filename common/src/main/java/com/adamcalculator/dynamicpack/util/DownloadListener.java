package com.adamcalculator.dynamicpack.util;

import java.util.OptionalLong;

public interface DownloadListener {
    void onStart();

    void onContentLength(OptionalLong contentLength);

    void onProgress(long total);

    void onFinish(boolean b);
}
