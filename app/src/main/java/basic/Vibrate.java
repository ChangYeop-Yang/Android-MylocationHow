package basic;

import android.content.Context;
import android.os.Vibrator;

/**
 * Created by Mari on 2015-08-27.
 */
public class Vibrate
{
    private static Vibrator mVidrator = null;

    public static final void Vibrator(Context mContext)
    {
        mVidrator = (android.os.Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
        /* Pattern */
        long[] mVibratePattern = {100, 100, 300};
        mVidrator.vibrate(300);
        mVidrator.vibrate(mVibratePattern, -1);
    }
}
