package Game;

import Enums.SoundType;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

import static Constants.CONST.*;

public class SoundEffects {
    public static void playSound(SoundType soundType) {
        try {
            File file = mapSoundWithFile(soundType);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            clip.drain(); // wait for the sound to finish
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static File mapSoundWithFile(SoundType soundType) {
        File file = null;
        switch (soundType) {
            case MOVE -> file = new File(MOVE_SOUND);
            case MOVE_CHECK -> file = new File(MOVE_CHECK_SOUND);
            case CAPTURE -> file = new File(CAPTURE_SOUND);
            case CASTLE -> file = new File(CASTLE_SOUND);
            case ILLEGAL -> file = new File(ILLEGAL_SOUND);
            case PROMOTE -> file = new File(PROMOTE_SOUND);
            case GAME_START -> file = new File(GAME_START_SOUND);
            case GAME_END -> file = new File(GAME_END_SOUND);
            case CLICK -> file = new File(CLICK_SOUND);
        }

        return file;
    }
}
