import java.util.*;

/** Mesin inferensi Forward Chaining (ditulis sendiri). */
public class InferenceEngine {

    public static class Hasil {
        public final List<String> log = new ArrayList<>();
        public final Set<String> memori = new LinkedHashSet<>();
        public final Map<String, Rule> asal = new LinkedHashMap<>(); // fakta turunan -> aturan penghasilnya
        public final List<String> kesimpulanAkhir = new ArrayList<>();
        public final List<String> kesimpulanAntara = new ArrayList<>();
    }

    public Hasil jalankan(KnowledgeBase kb, Set<String> faktaAwal) {
        Hasil h = new Hasil();
        h.memori.addAll(faktaAwal);

        int siklus = 0;
        while (true) {
            siklus++;
            // cocok = aturan yang syaratnya terpenuhi DAN hasilnya belum ada di memori
            Rule dipilih = null;
            for (Rule r : kb.aturan) {
                if (h.memori.contains(r.maka)) continue;
                if (r.terpenuhi(h.memori)) { dipilih = r; break; } // ambil aturan pertama
            }
            if (dipilih == null) {
                h.log.add("Siklus " + siklus + ": tidak ada aturan baru, berhenti");
                break;
            }
            h.memori.add(dipilih.maka);
            h.asal.put(dipilih.maka, dipilih);
            h.log.add("Siklus " + siklus + ": " + dipilih.id + " -> " + dipilih.maka);
        }

        // Kesimpulan akhir = fakta turunan yang tidak dipakai sebagai syarat aturan lain
        Set<String> dipakai = kb.faktaDipakaiSebagaiSyarat();
        for (String f : h.asal.keySet()) {
            if (dipakai.contains(f)) h.kesimpulanAntara.add(f);
            else h.kesimpulanAkhir.add(f);
        }
        return h;
    }

    /** Menyusun alasan secara rekursif: fakta <- aturan (syarat yang terpenuhi) [sumber]. */
    public void susunAlasan(String fakta, Hasil h, int tingkat, StringBuilder sb) {
        Rule r = h.asal.get(fakta);
        if (r == null) return; // fakta dasar (jawaban user), tidak ada alasan lebih dalam
        String indent = "  ".repeat(tingkat);
        sb.append(indent).append(fakta).append(" <- ").append(r.id)
          .append(" (").append(String.join("; ", r.syaratYangAda(h.memori))).append(")")
          .append(" [sumber ").append(String.join(", ", r.sumber)).append("]\n");
        for (String syarat : r.syaratYangAda(h.memori)) susunAlasan(syarat, h, tingkat + 1, sb);
    }
}
