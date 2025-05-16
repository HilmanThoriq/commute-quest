package com.example.commutequest;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.commutequest.adapter.MessageAdapter;
import com.example.commutequest.model.Message;
import com.example.commutequest.util.ChatService;

import java.util.ArrayList;
import java.util.List;

public class TanyaBusFragment extends Fragment {


    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ImageButton voiceInputButton;
    private ImageButton attachmentButton;
    private ProgressBar loadingIndicator;
    private TextView emptyStateText;

    private MessageAdapter messageAdapter;
    private ChatService chatService;
    private List<Message> messageList;

    public TanyaBusFragment() {
        // Required empty public constructor
    }

    public static TanyaBusFragment newInstance() {
        return new TanyaBusFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatService = ChatService.getInstance(this.getContext());
        messageList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tanya_bus, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        chatRecyclerView = view.findViewById(R.id.chat_recycler_view);
        messageInput = view.findViewById(R.id.message_input);
        sendButton = view.findViewById(R.id.send_button);
        voiceInputButton = view.findViewById(R.id.voice_input_button);
        attachmentButton = view.findViewById(R.id.attachment_button);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        emptyStateText = view.findViewById(R.id.empty_state_text);

        // Setup RecyclerView
        messageAdapter = new MessageAdapter();
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(messageAdapter);

        // Set click listeners
        setupClickListeners();

        // Load chat history or welcome messages
        loadChatHistory();
    }

    private void setupClickListeners() {
        // Send button click listener
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!TextUtils.isEmpty(message)) {
                sendMessage(message);
            }
        });

        // Voice input button click listener
        voiceInputButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Fitur input suara akan segera tersedia", Toast.LENGTH_SHORT).show();
        });

        // Attachment button click listener
        attachmentButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Fitur lampiran akan segera tersedia", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadChatHistory() {
        // Show loading indicator
        loadingIndicator.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);

        // Get chat history from API
        chatService.getChatHistory(new ChatService.ChatHistoryCallback() {
            @Override
            public void onHistoryLoaded(List<Message> messages) {
                messageList.clear();

                if (messages != null && !messages.isEmpty()) {
                    // If we have chat history, add it to the adapter
                    messageList.addAll(messages);
                    messageAdapter.setMessages(messageList);
                } else {
                    // If no history, show welcome messages
                    List<Message> welcomeMessages = chatService.getWelcomeMessages();
                    messageList.addAll(welcomeMessages);
                    messageAdapter.setMessages(messageList);
                }

                // Hide loading indicator
                loadingIndicator.setVisibility(View.GONE);

                // Update UI
                if (messageList.isEmpty()) {
                    emptyStateText.setVisibility(View.VISIBLE);
                } else {
                    emptyStateText.setVisibility(View.GONE);
                    chatRecyclerView.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onError(String errorMessage) {
                // On error, show welcome messages instead
                List<Message> welcomeMessages = chatService.getWelcomeMessages();
                messageList.addAll(welcomeMessages);
                messageAdapter.setMessages(messageList);

                // Hide loading indicator
                loadingIndicator.setVisibility(View.GONE);

                // Update UI
                if (messageList.isEmpty()) {
                    emptyStateText.setVisibility(View.VISIBLE);
                } else {
                    emptyStateText.setVisibility(View.GONE);
                    chatRecyclerView.scrollToPosition(messageList.size() - 1);
                }

                // Show error toast
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String messageText) {
        // Create and add user message
        Message userMessage = new Message(messageText, Message.TYPE_OUTGOING);
        messageList.add(userMessage);
        messageAdapter.setMessages(messageList); // Update entire list instead of adding individually

        // Clear input field
        messageInput.setText("");

        // Scroll to latest message
        chatRecyclerView.scrollToPosition(messageList.size() - 1);

        // Show typing indicator
        showTypingIndicator(true);

        // Get response from service
        chatService.sendMessage(messageText, new ChatService.ChatCallback() {
            @Override
            public void onResponse(Message responseMessage) {
                // Hide typing indicator
                showTypingIndicator(false);

                // Add response message
                messageList.add(responseMessage);
                messageAdapter.setMessages(messageList); // Update entire list instead of adding individually

                // Scroll to latest message
                chatRecyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onError(String errorMessage) {
                // Hide typing indicator
                showTypingIndicator(false);

                // Show error toast
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTypingIndicator(boolean show) {
        // In a real app, you would add a typing indicator view
        loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}