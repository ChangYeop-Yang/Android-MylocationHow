package basic;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

/**
 * Created by Mari on 2015-08-26.
 */
public class Sound
{
    /* Context */
    private Context mContext = null;

    /* int */
    private int mSound = 0;

    /* SoundPool */
    private SoundPool mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

    public Sound(Context mContext, int mSound)
    {
        this.mContext = mContext;
        this.mSound = mSoundPool.load(mContext, mSound, 1);
    }
    /* PlaySound */
    public final void PlaySound() { mSoundPool.play(mSound, 1, 1, 0, 0, 1); }
}
