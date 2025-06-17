/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package register4;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class Message {

      static String getFullReport() {
        StringBuilder report = new StringBuilder();
        report.append("==== Message Report ====\n");
        report.append("Total Messages Sent: ").append(totalMessagesSent).append("\n\n");

        report.append("--- Sent Messages ---\n");
        for (Message m : sentMessages) {
            report.append(m.toString()).append("\n\n");
        }

        report.append("--- Disregarded Messages ---\n");
        for (Message m : disregardedMessages) {
            report.append(m.toString()).append("\n\n");
        }

        report.append("--- Stored Messages ---\n");
        for (Message m : storedMessages) {
            report.append(m.toString()).append("\n\n");
        }

        return report.toString();
    }

   static Set<String> getSenderRecipientList() {
        Set<String> pairs = new HashSet<>();
        for (Message m : sentMessages) {
            pairs.add("From: " + m.getSender() + " -> To: " + m.getRecipient());
        }
        return pairs;
    }

     static void displaySenderRecipientList() {
        Set<String> pairs = getSenderRecipientList();
        System.out.println("=== Sender-Recipient Pairs ===");
        for (String pair : pairs) {
            System.out.println(pair);
        }
    }
     
    private String sender;
    private String recipient;
    private String content;
    private String messageID;

    private static int totalMessagesSent = 0;

    private static List<Message> sentMessages = new ArrayList<>();
    private static List<Message> disregardedMessages = new ArrayList<>();
    private static List<Message> storedMessages = new ArrayList<>();
    private static List<String> messageHashes = new ArrayList<>();
    private static List<String> messageIDs = new ArrayList<>();

    private static Map<String, Message> messageRegistry = new HashMap<>();
    private static Queue<Integer> availableIDs = new LinkedList<>();

    static {
        for (int i = 1; i <= 10; i++) {
            availableIDs.offer(i);
        }
        loadStoredMessagesFromJSON();
    }

    public Message(String sender, String recipient, String content) {
        if (availableIDs.isEmpty()) throw new IllegalStateException("Max 10 messages allowed.");

        this.sender = sender;
        this.recipient = recipient;
        this.content = content;

        int nextID = availableIDs.poll();
        this.messageID = String.valueOf(nextID);

        messageRegistry.put(this.messageID, this);
        messageIDs.add(this.messageID);
        messageHashes.add(createMessageHash());
    }

    public String createMessageHash() {
        return Integer.toString(content.hashCode());
    }

    public static boolean checkRecipientCell(String cellNumber) {
        return cellNumber != null && cellNumber.matches("^\\+27\\d{9}$");
    }

    public static boolean checkMessageContentLength(String messageContent) {
        return messageContent != null && messageContent.length() <= 250;
    }

    public static void sendMessage(Message msg) {
        sentMessages.add(msg);
        // Optionally add to storedMessages if you want to keep sent messages stored
        storedMessages.add(msg);
        totalMessagesSent++;
        saveStoredMessagesToJSON();
    }

    public static void storeMessage(Message msg) {
        storedMessages.add(msg);
        messageRegistry.put(msg.getMessageID(), msg);
        if (!messageIDs.contains(msg.getMessageID())) {
            messageIDs.add(msg.getMessageID());
        }
        if (!messageHashes.contains(msg.createMessageHash())) {
            messageHashes.add(msg.createMessageHash());
        }
        saveStoredMessagesToJSON();
    }

    public static void disregardMessage(Message msg) {
        disregardedMessages.add(msg);

        // Remove from all lists and registry
        sentMessages.remove(msg);
        storedMessages.remove(msg);
        messageRegistry.remove(msg.getMessageID());
        messageHashes.remove(msg.createMessageHash());
        messageIDs.remove(msg.getMessageID());

        // Recycle the ID for reuse
        availableIDs.offer(Integer.parseInt(msg.getMessageID()));

        saveStoredMessagesToJSON();
    }

    public static boolean deleteMessageByHash(String hash) {
        for (Message msg : new ArrayList<>(sentMessages)) {
            if (msg.createMessageHash().equals(hash)) {
                sentMessages.remove(msg);
                storedMessages.remove(msg);
                messageRegistry.remove(msg.getMessageID());
                messageHashes.remove(hash);
                messageIDs.remove(msg.getMessageID());
                availableIDs.offer(Integer.parseInt(msg.getMessageID()));
                saveStoredMessagesToJSON();
                return true;
            }
        }
        return false;
    }

    public static Message getMessageByID(String id) {
        return messageRegistry.get(id);
    }

    public static List<Message> searchMessagesByRecipient(String recipient) {
        List<Message> results = new ArrayList<>();
        for (Message m : sentMessages) {
            if (m.getRecipient().equalsIgnoreCase(recipient)) {
                results.add(m);
            }
        }
        return results;
    }

    public static Message getLongestMessage() {
        Message longest = null;
        for (Message m : sentMessages) {
            if (longest == null || m.getContent().length() > longest.getContent().length()) {
                longest = m;
            }
        }
        return longest;
    }

    public static void saveStoredMessagesToJSON() {
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter("storedMessages.json")) {
            gson.toJson(storedMessages, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadStoredMessagesFromJSON() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("storedMessages.json")) {
            Type listType = new TypeToken<List<Message>>() {}.getType();
            List<Message> loaded = gson.fromJson(reader, listType);
            if (loaded != null) {
                storedMessages.clear();
                messageRegistry.clear();
                messageIDs.clear();
                messageHashes.clear();
                availableIDs.clear();

                // Reinitialize available IDs 1-10
                for (int i = 1; i <= 10; i++) {
                    availableIDs.offer(i);
                }

                storedMessages.addAll(loaded);

                // Remove assigned IDs from availableIDs queue
                for (Message m : loaded) {
                    messageRegistry.put(m.getMessageID(), m);
                    messageIDs.add(m.getMessageID());
                    messageHashes.add(m.createMessageHash());

                    int assignedID = Integer.parseInt(m.getMessageID());
                    // Remove assignedID from availableIDs
                    availableIDs.remove(assignedID);
                }
            }
        } catch (IOException e) {
            System.out.println("No stored messages found yet.");
        }
    }

    public static List<Message> getSentMessages() { return sentMessages; }
    public static List<Message> getDisregardedMessages() { return disregardedMessages; }
    public static List<Message> getStoredMessages() { return storedMessages; }
    public static List<String> getMessageHashes() { return messageHashes; }
    public static List<String> getMessageIDs() { return messageIDs; }
    public static int returnTotalMessages() { return totalMessagesSent; }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getContent() {
        return content;
    }

    public String getMessageID() {
        return messageID;
    }

    @Override
    public String toString() {
        return "Message ID: " + messageID +
                "\nFrom: " + sender +
                "\nTo: " + recipient +
                "\nMessage: " + content +
                "\nHash: " + createMessageHash();
    }
}