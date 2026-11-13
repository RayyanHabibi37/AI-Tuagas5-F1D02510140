import java.util.*;

/** Parser JSON sederhana (tanpa library luar). Object -> Map, array -> List, string -> String. */
public class JsonParser {
    private final String s;
    private int i = 0;

    private JsonParser(String s) { this.s = s; }

    public static Object parse(String teks) {
        JsonParser p = new JsonParser(teks);
        Object v = p.nilai();
        p.lewatiSpasi();
        if (p.i != p.s.length()) throw p.galat("Ada teks berlebih setelah JSON selesai");
        return v;
    }

    private Object nilai() {
        lewatiSpasi();
        if (i >= s.length()) throw galat("JSON berakhir terlalu cepat");
        char c = s.charAt(i);
        if (c == '{') return objek();
        if (c == '[') return larik();
        if (c == '"') return string();
        if (s.startsWith("true", i))  { i += 4; return Boolean.TRUE; }
        if (s.startsWith("false", i)) { i += 5; return Boolean.FALSE; }
        if (s.startsWith("null", i))  { i += 4; return null; }
        return angka();
    }

    private Map<String, Object> objek() {
        Map<String, Object> m = new LinkedHashMap<>();
        i++; // lewati '{'
        lewatiSpasi();
        if (intip() == '}') { i++; return m; }
        while (true) {
            lewatiSpasi();
            String kunci = string();
            lewatiSpasi();
            harap(':');
            m.put(kunci, nilai());
            lewatiSpasi();
            char c = ambil();
            if (c == ',') continue;
            if (c == '}') return m;
            throw galat("Diharapkan ',' atau '}'");
        }
    }

    private List<Object> larik() {
        List<Object> l = new ArrayList<>();
        i++; // lewati '['
        lewatiSpasi();
        if (intip() == ']') { i++; return l; }
        while (true) {
            l.add(nilai());
            lewatiSpasi();
            char c = ambil();
            if (c == ',') continue;
            if (c == ']') return l;
            throw galat("Diharapkan ',' atau ']'");
        }
    }

    private String string() {
        harap('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (i >= s.length()) throw galat("String tidak ditutup");
            char c = s.charAt(i++);
            if (c == '"') return sb.toString();
            if (c != '\\') { sb.append(c); continue; }
            char e = ambil();
            switch (e) {
                case '"': sb.append('"'); break;
                case '\\': sb.append('\\'); break;
                case '/': sb.append('/'); break;
                case 'b': sb.append('\b'); break;
                case 'f': sb.append('\f'); break;
                case 'n': sb.append('\n'); break;
                case 'r': sb.append('\r'); break;
                case 't': sb.append('\t'); break;
                case 'u':
                    if (i + 4 > s.length()) throw galat("Escape \\u tidak lengkap");
                    sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                    i += 4;
                    break;
                default: throw galat("Escape tidak dikenal: \\" + e);
            }
        }
    }

    private Double angka() {
        int mulai = i;
        while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
        if (mulai == i) throw galat("Nilai JSON tidak dikenali");
        return Double.valueOf(s.substring(mulai, i));
    }

    private void lewatiSpasi() {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
    }

    private char intip() {
        if (i >= s.length()) throw galat("JSON berakhir terlalu cepat");
        return s.charAt(i);
    }

    private char ambil() {
        char c = intip();
        i++;
        return c;
    }

    private void harap(char c) {
        if (ambil() != c) { i--; throw galat("Diharapkan '" + c + "'"); }
    }

    private RuntimeException galat(String pesan) {
        int baris = 1;
        for (int k = 0; k < Math.min(i, s.length()); k++) if (s.charAt(k) == '\n') baris++;
        return new IllegalArgumentException("JSON salah di baris " + baris + ": " + pesan);
    }
}
