package me.mrkirby153.kcutils;

import com.google.common.collect.ImmutableList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Fetches player UUIDs from their minecraft username
 */
public class UUIDFetcher implements Callable<Map<String, UUID>> {

    private static final double PROFILES_PER_REQUEST = 100;
    private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
    private final JSONParser jsonParser = new JSONParser();
    private final List<String> names;
    private final boolean rateLimiting;

    public UUIDFetcher(List<String> names, boolean rateLimiting) {
        this.names = ImmutableList.copyOf(names);
        this.rateLimiting = rateLimiting;
    }

    public UUIDFetcher(List<String> names) {
        this(names, true);
    }

    /**
     * Creates the connection to Mojang's servers
     *
     * @return A connection
     * @throws IOException If an error occurred when connecting
     */
    private static HttpURLConnection createConnection() throws IOException {
        URL url = new URL(PROFILE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }

    /**
     * Loads a UUID from a byte array
     *
     * @param array The array
     * @return A UUID from the bytes
     */
    public static UUID fromBytes(byte[] array) {
        if (array.length != 16) {
            throw new IllegalArgumentException("Illegal byte array length: " + array.length);
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(array);
        long mostSignificant = byteBuffer.getLong();
        long leastSignificant = byteBuffer.getLong();
        return new UUID(mostSignificant, leastSignificant);
    }

    /**
     * Converts a UUID <b>WITHOUT DASHES</b> into a UUID object
     *
     * @param id The UUID
     * @return The UUD object
     */
    private static UUID getUUID(String id) {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
    }

    /**
     * Gets the UUID of a given player's name
     *
     * @param name The username to get UUID of
     * @return The player's UUID
     * @throws IOException If an error occurres
     */
    public static UUID getUUIDOf(String name) throws IOException {
        return new UUIDFetcher(Collections.singletonList(name)).call().get(name);
    }

    /**
     * Retrieve the UUID of a player async from Mojang. <b>This does properly handle rate limits</b>
     *
     * @param name     The name of the player to retrieve
     * @param callback The callback called when the UUID is found
     */
    public static void getUUIDOfAsync(String name, Callback<UUID> callback) {
        new Thread(() -> callback.call(new UUIDFetcher(Collections.singletonList(name), true).call().get(name))).start();
    }

    /**
     * Retrieve multiple UUIDs of a player asynchronously from Mojang's servers. <b>This does properly handle rate limits</b>
     *
     * @param names    The names of the players to retrieve
     * @param callback The callback called when thte UUID is found.
     */
    public static void getUUIDsAsync(List<String> names, Callback<Map<String, UUID>> callback) {
        new Thread(() -> callback.call(new UUIDFetcher(names, true).call())).start();
    }

    /**
     * Converts a UUID to a byte array: MSB then LSB
     *
     * @param uuid The UUID to convert
     * @return A byte array
     */
    public static byte[] toBytes(UUID uuid) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }

    /**
     * Writes data to a connection
     *
     * @param connection The connection to write data to
     * @param body       The body of the connection
     * @throws IOException If an error occurres
     */
    private static void writeBody(HttpURLConnection connection, String body) throws IOException {
        OutputStream stream = connection.getOutputStream();
        stream.write(body.getBytes());
        stream.flush();
        stream.close();
    }

    /**
     * Fetches the UUIDs
     *
     * @return A list of UUIDs
     */
    public Map<String, UUID> call() {
        try {
            Map<String, UUID> uuidMap = new HashMap<>();
            int requests = (int) Math.ceil(names.size() / PROFILES_PER_REQUEST);
            for (int i = 0; i < requests; i++) {
                HttpURLConnection connection = createConnection();
                String body = JSONArray.toJSONString(names.subList(i * 100, Math.min((i + 1) * 100, names.size())));
                writeBody(connection, body);
                JSONArray array = (JSONArray) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
                for (Object profile : array) {
                    JSONObject jsonProfile = (JSONObject) profile;
                    String id = (String) jsonProfile.get("id");
                    String name = (String) jsonProfile.get("name");
                    UUID uuid = UUIDFetcher.getUUID(id);
                    uuidMap.put(name, uuid);
                }
                if (rateLimiting && i != requests - 1) {
                    Thread.sleep(100L);
                }
            }
            return uuidMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
