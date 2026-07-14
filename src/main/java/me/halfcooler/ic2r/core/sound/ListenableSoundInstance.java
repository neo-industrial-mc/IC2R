package me.halfcooler.ic2r.core.sound;

public interface ListenableSoundInstance
{
	void addOnFinishListener(Runnable var1);

	void onFinish(Runnable var1);

	void clearOnFinishListener();

	void finish();
}
