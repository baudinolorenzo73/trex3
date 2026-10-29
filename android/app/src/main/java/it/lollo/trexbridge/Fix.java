package it.lollo.trexbridge;

import java.util.Map;
import java.util.regex.Pattern;

final class Fix {
    static final Pattern SESSION = Pattern.compile("[a-zA-Z0-9_-]{1,64}");
    final String session;
    final long seq, observedAt, receivedAt;
    final double lat, lon;

    Fix(Map<String,String> p, long now) {
        if (!"1".equals(p.get("v")) || !"true".equals(p.get("valid"))) throw new IllegalArgumentException("Posizione non valida");
        session = p.get("session");
        if (session == null || !SESSION.matcher(session).matches()) throw new IllegalArgumentException("Sessione non valida");
        seq = Long.parseLong(p.get("seq"));
        observedAt = Long.parseLong(p.get("observedAt"));
        lat = Double.parseDouble(p.get("lat")); lon = Double.parseDouble(p.get("lon"));
        if (seq < 1 || seq > 9007199254740991L || !Double.isFinite(lat) || !Double.isFinite(lon) || Math.abs(lat) > 90 || Math.abs(lon) > 180)
            throw new IllegalArgumentException("Coordinate o sequenza non valide");
        if (observedAt < now - 15000 || observedAt > now + 120000)
            throw new IllegalArgumentException("Posizione scaduta: verifica ora di tablet e orologio");
        receivedAt = now;
    }
    String key() { return session + ":" + seq; }
    boolean fresh(long now) { return now >= receivedAt && now - receivedAt < 8000; }
}
