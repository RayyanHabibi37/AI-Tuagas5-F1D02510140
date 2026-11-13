import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

/**
 * Sistem Pakar Diagnosa Penyakit Ikan Cupang (Forward Chaining).
 * Jalankan: java Main [file_kb.json] [--echo]
 */
public class Main {

    public static void main(String[] args) throws Exception {
        String berkas = "kb_cupang.json";
        boolean echo = false;
        for (String a : args) {
            if (a.equals("--echo")) echo = true;
            else berkas = a;
        }

        KnowledgeBase kb;
        try {
            kb = KnowledgeBase.muat(Path.of(berkas));
        } catch (Exception e) {
            System.out.println("Gagal memuat basis pengetahuan '" + berkas + "': " + e.getMessage());
            return;
        }

        Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);
        System.out.println(kb.judul);
        System.out.println("Jawab y/t.");

        // 1. Tanyakan fakta dasar -> memori awal
        Set<String> memoriAwal = new LinkedHashSet<>();
        int no = 1;
        for (String fakta : kb.faktaDasar()) {
            if (tanya(in, no++, fakta, echo)) memoriAwal.add(fakta);
        }

        // 2. Jalankan forward chaining
        InferenceEngine mesin = new InferenceEngine();
        InferenceEngine.Hasil h = mesin.jalankan(kb, memoriAwal);

        // 3. Tampilkan proses, kesimpulan, dan alasan
        System.out.println();
        System.out.println("Fakta awal: " + (memoriAwal.isEmpty() ? "(tidak ada)" : String.join(", ", memoriAwal)));
        System.out.println();
        for (String baris : h.log) System.out.println(baris);
        System.out.println();

        if (h.kesimpulanAkhir.isEmpty()) {
            System.out.println("Kesimpulan: tidak ada kesimpulan yang dapat ditarik dari gejala yang dipilih");
            if (!h.kesimpulanAntara.isEmpty())
                System.out.println("Temuan antara: " + String.join(", ", h.kesimpulanAntara));
            System.out.println("Saran: lengkapi pengamatan atau konsultasikan ke dokter hewan ikan.");
            System.out.println();
        } else {
            for (String k : h.kesimpulanAkhir) {
                System.out.println("Kesimpulan: " + k);
                System.out.println("Alasan:");
                StringBuilder sb = new StringBuilder();
                mesin.susunAlasan(k, h, 1, sb);
                System.out.print(sb);
                System.out.println();
            }
            Set<String> dipakai = new LinkedHashSet<>();
            for (Rule r : h.asal.values()) dipakai.addAll(r.sumber);
            System.out.println("Sumber yang dipakai:");
            for (String s : dipakai) System.out.println("  " + s + ": " + kb.sumber.get(s));
            System.out.println();
        }
        System.out.println("Catatan: " + kb.batas);
    }

    private static boolean tanya(Scanner in, int no, String fakta, boolean echo) {
        while (true) {
            System.out.print(no + ". Apakah " + fakta + "? (y/t) > ");
            if (!in.hasNextLine()) { System.out.println("t"); return false; }
            String j = in.nextLine().trim().toLowerCase();
            if (echo) System.out.println(j);
            if (j.equals("y") || j.equals("ya")) return true;
            if (j.equals("t") || j.equals("tidak") || j.equals("n")) return false;
            System.out.println("   Jawab dengan y atau t.");
        }
    }
}
