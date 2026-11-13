import java.util.*;

/** Satu aturan IF-THEN. mode "dan" = semua syarat harus ada, "atau" = salah satu cukup. */
public class Rule {
    public final String id;
    public final List<String> jika;
    public final String mode;
    public final String maka;
    public final List<String> sumber;

    public Rule(String id, List<String> jika, String mode, String maka, List<String> sumber) {
        this.id = id;
        this.jika = jika;
        this.mode = mode;
        this.maka = maka;
        this.sumber = sumber;
    }

    /** Apakah syarat aturan ini terpenuhi oleh isi memori kerja? */
    public boolean terpenuhi(Set<String> memori) {
        if (mode.equals("dan")) {
            for (String syarat : jika) if (!memori.contains(syarat)) return false;
            return true;
        }
        for (String syarat : jika) if (memori.contains(syarat)) return true;
        return false;
    }

    /** Syarat-syarat yang benar-benar ada di memori (dipakai untuk menyusun alasan). */
    public List<String> syaratYangAda(Set<String> memori) {
        List<String> hasil = new ArrayList<>();
        for (String syarat : jika) if (memori.contains(syarat)) hasil.add(syarat);
        return hasil;
    }
}
