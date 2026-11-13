import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Memuat dan memvalidasi file basis pengetahuan (JSON). */
public class KnowledgeBase {
    public String judul;
    public String batas;
    public Map<String, String> sumber = new LinkedHashMap<>();
    public List<Rule> aturan = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public static KnowledgeBase muat(Path file) throws IOException {
        String teks = Files.readString(file, StandardCharsets.UTF_8);
        Map<String, Object> akar = (Map<String, Object>) JsonParser.parse(teks);

        KnowledgeBase kb = new KnowledgeBase();
        kb.judul = (String) akar.get("judul");
        kb.batas = (String) akar.get("batas");
        for (Map.Entry<String, Object> e : ((Map<String, Object>) akar.get("sumber")).entrySet())
            kb.sumber.put(e.getKey(), (String) e.getValue());

        for (Object o : (List<Object>) akar.get("aturan")) {
            Map<String, Object> m = (Map<String, Object>) o;
            List<String> jika = new ArrayList<>();
            for (Object x : (List<Object>) m.get("jika")) jika.add((String) x);
            List<String> sumberAturan = new ArrayList<>();
            Object s = m.get("sumber");
            if (s instanceof List) for (Object x : (List<Object>) s) sumberAturan.add((String) x);
            else if (s != null) sumberAturan.add((String) s);
            kb.aturan.add(new Rule((String) m.get("id"), jika, (String) m.get("mode"),
                    (String) m.get("maka"), sumberAturan));
        }
        kb.validasi();
        return kb;
    }

    private void validasi() {
        if (aturan.size() < 5) throw new IllegalStateException("Minimal 5 aturan");
        Set<String> kesimpulan = new HashSet<>();
        for (Rule r : aturan) {
            if (r.jika.isEmpty()) throw new IllegalStateException(r.id + ": syarat kosong");
            if (!r.mode.equals("dan") && !r.mode.equals("atau"))
                throw new IllegalStateException(r.id + ": mode harus 'dan' atau 'atau'");
            if (r.sumber.isEmpty()) throw new IllegalStateException(r.id + ": tidak punya sumber");
            for (String s : r.sumber)
                if (!sumber.containsKey(s)) throw new IllegalStateException(r.id + ": sumber " + s + " tidak terdaftar");
            kesimpulan.add(r.maka);
        }
        if (kesimpulan.size() < 3) throw new IllegalStateException("Minimal 3 kesimpulan berbeda");
    }

    /** Semua teks fakta yang muncul sebagai syarat di salah satu aturan. */
    public Set<String> faktaDipakaiSebagaiSyarat() {
        Set<String> hasil = new LinkedHashSet<>();
        for (Rule r : aturan) hasil.addAll(r.jika);
        return hasil;
    }

    /** Fakta yang tidak pernah menjadi hasil aturan -> ditanyakan ke user, urut kemunculan. */
    public List<String> faktaDasar() {
        Set<String> hasilAturan = new HashSet<>();
        for (Rule r : aturan) hasilAturan.add(r.maka);
        List<String> dasar = new ArrayList<>();
        for (String f : faktaDipakaiSebagaiSyarat()) if (!hasilAturan.contains(f)) dasar.add(f);
        return dasar;
    }
}
