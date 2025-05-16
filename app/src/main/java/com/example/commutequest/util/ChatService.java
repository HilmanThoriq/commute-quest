package com.example.commutequest.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.commutequest.model.ChatHistoryResponse;
import com.example.commutequest.model.Message;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Chat service that connects to the real API with fallback to simulated responses
 */
public class ChatService {
    private static final String TAG = "ChatService";
    private static final String API_URL = "https://commute.disyfa.site/api/chat";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Singleton instance
    private static ChatService instance;

    // Dummy data for fallback - common bus inquiries and responses
    private final Map<String, List<String>> dummyResponses;
    private final List<String> defaultResponses;
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());

    // OkHttp client for API calls
    private final OkHttpClient client;

    // Session manager for auth token
    private SessionManager sessionManager;

    // Context for accessing session manager
    private Context context;

    // Interface for callback
    public interface ChatCallback {
        void onResponse(Message message);
        void onError(String errorMessage);
    }

    // Interface for history callback
    public interface ChatHistoryCallback {
        void onHistoryLoaded(List<Message> messages);
        void onError(String errorMessage);
    }

    // Get singleton instance
    public static synchronized ChatService getInstance(Context context) {
        if (instance == null) {
            instance = new ChatService(context);
        }
        return instance;
    }

    /**
     * Private constructor that takes a context
     */
    private ChatService(Context context) {
        this.context = context;
        this.sessionManager = new SessionManager(context);
        this.client = new OkHttpClient();

        // Initialize dummy response data for fallback
        dummyResponses = new HashMap<>();

        // Add common inquiries and responses
        dummyResponses.put("jadwal", Arrays.asList(
                "Jadwal bus tersedia setiap 30 menit dari pukul 05:00 hingga 22:00.",
                "Bus beroperasi dari jam 05:00 pagi hingga 22:00 malam dengan interval 30 menit.",
                "Anda dapat melihat jadwal lengkap di aplikasi atau website kami."
        ));

        dummyResponses.put("rute", Arrays.asList(
                "Rute bus meliputi Terminal A - Jalan Utama - Pasar Baru - Terminal B.",
                "Bus kami melewati beberapa titik penting seperti stasiun kereta, mall, dan rumah sakit.",
                "Untuk detail rute lengkap, silakan periksa menu Peta di aplikasi."
        ));

        dummyResponses.put("tiket", Arrays.asList(
                "Harga tiket bus adalah Rp 5.000 untuk sekali perjalanan.",
                "Anda dapat membeli tiket langsung di bus atau melalui aplikasi ini.",
                "Tersedia paket langganan dengan diskon 20% untuk penggunaan rutin."
        ));

        dummyResponses.put("lokasi", Arrays.asList(
                "Anda dapat melacak lokasi bus secara real-time melalui fitur 'Pelacak Bus' di aplikasi.",
                "Bus terdekat dari lokasi Anda saat ini berjarak sekitar 2 km.",
                "Gunakan fitur GPS di aplikasi untuk melihat posisi bus terdekat."
        ));

        dummyResponses.put("pembayaran", Arrays.asList(
                "Metode pembayaran yang tersedia: tunai, e-wallet, dan kartu elektronik.",
                "Anda dapat melakukan top-up saldo bus card melalui aplikasi ini.",
                "Untuk pembayaran non-tunai, tempelkan kartu Anda pada mesin pembaca di dalam bus."
        ));

        // Default responses for unknown queries
        defaultResponses = Arrays.asList(
                "Maaf, saya tidak memahami pertanyaan Anda. Silakan coba pertanyaan lain terkait jadwal, rute, tiket, lokasi, atau pembayaran.",
                "Saya belum bisa menjawab pertanyaan tersebut. Coba tanyakan tentang jadwal bus, rute perjalanan, atau informasi tiket.",
                "Mohon maaf, pertanyaan Anda di luar cakupan informasi yang saya miliki. Silakan hubungi customer service kami di 0800-123-BUS untuk bantuan lebih lanjut."
        );
    }

    /**
     * Send a message and get a response from the API
     * Falls back to simulated responses if API call fails
     * @param userMessage User's message
     * @param callback Callback for response
     */
    public void sendMessage(String userMessage, ChatCallback callback) {
        try {
            // Create JSON request body
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("text", userMessage);

            // Create request
            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request.Builder requestBuilder = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .addHeader("accept", "application/json")
                    .addHeader("Content-Type", "application/json");

            // Add auth token if available
            String authToken = sessionManager.getAuthToken();
            if (authToken != null && !authToken.isEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer " + authToken);
            }

            // Add CSRF token header (empty in this example as per the curl example)
            requestBuilder.addHeader("X-CSRF-TOKEN", "");

            Request request = requestBuilder.build();

            // Execute request asynchronously
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "API call failed, using fallback response", e);
                    // Use fallback response if API call fails
                    useFallbackResponse(userMessage, callback);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (!response.isSuccessful()) {
                            Log.e(TAG, "Server error: " + response.code() + ", using fallback response");
                            // Use fallback if server returns error
                            useFallbackResponse(userMessage, callback);
                            return;
                        }

                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        // Parse the response based on the actual API response structure
                        boolean success = jsonResponse.optBoolean("success", false);

                        if (success) {
                            JSONObject data = jsonResponse.optJSONObject("data");
                            String messageText = data != null ? data.optString("answer", "No answer received") : "No data received";

                            // Create a message object and deliver via callback
                            Message responseMessage = new Message(messageText, Message.TYPE_INCOMING);
                            handler.post(() -> callback.onResponse(responseMessage));
                        } else {
                            String errorMsg = jsonResponse.optString("message", "Unknown error");
                            Log.e(TAG, "API error: " + errorMsg + ", using fallback response");
                            // Use fallback if API returns error
                            useFallbackResponse(userMessage, callback);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error, using fallback response", e);
                        // Use fallback if response parsing fails
                        useFallbackResponse(userMessage, callback);
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON request, using fallback response", e);
            // Use fallback if creating request fails
            useFallbackResponse(userMessage, callback);
        }
    }

    /**
     * Retrieves chat history from the API
     * @param callback Callback for response
     */
    public void getChatHistory(ChatHistoryCallback callback) {
        // Create request
        Request.Builder requestBuilder = new Request.Builder()
                .url(API_URL)
                .get()
                .addHeader("accept", "application/json");

        // Add auth token if available
        String authToken = sessionManager.getAuthToken();
        if (authToken != null && !authToken.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + authToken);
        }

        // Add CSRF token header (empty in this example as per the curl example)
        requestBuilder.addHeader("X-CSRF-TOKEN", "");

        Request request = requestBuilder.build();

        // Execute request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to get chat history", e);
                handler.post(() -> callback.onError("Failed to retrieve chat history: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Server error: " + response.code());
                    handler.post(() -> callback.onError("Server error: " + response.code()));
                    return;
                }

                String responseBody = response.body().string();
                try {
                    Gson gson = new Gson();
                    ChatHistoryResponse historyResponse = gson.fromJson(responseBody, ChatHistoryResponse.class);

                    if (historyResponse.isSuccess() && historyResponse.getData() != null) {
                        List<Message> messages = new ArrayList<>();

                        for (ChatHistoryResponse.ChatEntry entry : historyResponse.getData()) {
                            // Add user's question
                            Message questionMessage = new Message(entry.getQuestion(), Message.TYPE_OUTGOING);
                            messages.add(questionMessage);

                            // Add assistant's answer
                            Message answerMessage = new Message(entry.getAnswer(), Message.TYPE_INCOMING);
                            messages.add(answerMessage);
                        }

                        handler.post(() -> callback.onHistoryLoaded(messages));
                    } else {
                        handler.post(() -> callback.onError("Failed to parse chat history"));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing chat history response", e);
                    handler.post(() -> callback.onError("Error parsing response: " + e.getMessage()));
                }
            }
        });
    }

    /**
     * Use the fallback dummy response system when API is unavailable
     */
    private void useFallbackResponse(String userMessage, ChatCallback callback) {
        // Simulate network delay between 500ms - 1500ms for realistic feel
        int delay = 500 + random.nextInt(1000);

        handler.postDelayed(() -> {
            try {
                // Generate response based on keywords in user message
                String response = generateFallbackResponse(userMessage.toLowerCase());
                Message responseMessage = new Message(response, Message.TYPE_INCOMING);
                callback.onResponse(responseMessage);
            } catch (Exception e) {
                callback.onError("Terjadi kesalahan: " + e.getMessage());
            }
        }, delay);
    }

    /**
     * Generate a fallback response based on keywords in the user message
     */
    private String generateFallbackResponse(String userMessage) {
        // Check for keywords in the user message
        for (String keyword : dummyResponses.keySet()) {
            if (userMessage.contains(keyword)) {
                List<String> responses = dummyResponses.get(keyword);
                return responses.get(random.nextInt(responses.size()));
            }
        }

        // If no keyword matches, return a default response
        return defaultResponses.get(random.nextInt(defaultResponses.size()));
    }

    /**
     * Get predefined welcome messages
     * @return List of welcome messages
     */
    public List<Message> getWelcomeMessages() {
        List<Message> welcomeMessages = new ArrayList<>();
        welcomeMessages.add(new Message("Selamat datang di layanan Bus Assistant!", Message.TYPE_INCOMING));
        welcomeMessages.add(new Message("Saya akan membantu menjawab pertanyaan Anda seputar layanan bus.", Message.TYPE_INCOMING));
        welcomeMessages.add(new Message("Silakan tanyakan tentang jadwal, rute, tiket, lokasi bus, atau metode pembayaran.", Message.TYPE_INCOMING));
        return welcomeMessages;
    }
}