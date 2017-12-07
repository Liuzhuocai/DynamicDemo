package com.dynamic.liuzuo.dynamicicon.provider;

import android.content.Context;
import android.widget.TextView;

/**
 * Created by liuzuo on 17-4-1.
 */

public interface IDynamic {
    boolean init(Context context, TextView bubbleTextView);
    void removeDynamicReceiver();
    boolean updateDynamicIcon(boolean register);
    void clearDynamicIcon();
}
