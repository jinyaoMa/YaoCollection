package ca.jinyao.ma.yaocollection.audio.animators;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.annotation.Nullable;
import android.view.animation.LinearInterpolator;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Class AudioVolumeAnimator
 * create by jinyaoMa 0012 2018/8/12 19:52
 */
public class AudioVolumeAnimator {
    private IjkMediaPlayer ijkMediaPlayer;
    private VolumeListener volumeListener;
    private Listener listener;

    public interface VolumeListener {
        void onCompleted(IjkMediaPlayer ijkMediaPlayer);
    }

    public void gradient(float from, float to, IjkMediaPlayer ijkMediaPlayer) {
        gradient(from, to, ijkMediaPlayer, null);
    }

    public void gradient(float from, float to, IjkMediaPlayer ijkMediaPlayer, @Nullable VolumeListener volumeListener) {
        this.ijkMediaPlayer = ijkMediaPlayer;
        this.volumeListener = volumeListener;
        this.listener = new Listener(from, to);
        ValueAnimator animator = ValueAnimator.ofFloat(from, to);
        animator.setDuration(500);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(listener);
        animator.addListener(listener);
        animator.start();
    }

    private class Listener implements ValueAnimator.AnimatorUpdateListener,
            Animator.AnimatorListener{
        private float from;
        private float to;

        public Listener(float from, float to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float volume = (float) animation.getAnimatedValue();
            try {
                ijkMediaPlayer.setVolume(volume, volume);
            } catch (Exception e) {
                e.printStackTrace();
                animation.cancel();
            }
        }

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            try {
                ijkMediaPlayer.setVolume(to, to);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (volumeListener != null) {
                volumeListener.onCompleted(ijkMediaPlayer);
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            try {
                ijkMediaPlayer.setVolume(from, from);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }
}
