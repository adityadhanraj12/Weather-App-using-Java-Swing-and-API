import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.swing.*;

public class WeatherApp {

    private static final String API_KEY = "5a11b8eb8b4618c9c2884d73c289227d";
    private static final String API_URL = "http://api.openweathermap.org/data/2.5/weather?q=";
    private static final String[] MAJOR_CITIES = {"New York", "London", "Tokyo", "Paris", "Sydney"};
    private static JTextArea resultArea;
    private static JTextField cityField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WeatherApp::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Weather App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Background
        ImageIcon backgroundIcon = new ImageIcon("C:\\Users\\DELL\\OneDrive\\Desktop\\telebot\\Apple-Weather-app.webp");
        Image img = backgroundIcon.getImage().getScaledInstance(800, 600, Image.SCALE_SMOOTH);
        backgroundIcon = new ImageIcon(img);
        JLabel backgroundLabel = new JLabel(backgroundIcon);
        backgroundLabel.setLayout(new BorderLayout());
        frame.setContentPane(backgroundLabel);

        // Transparent Panel
        JPanel transparentPanel = new JPanel(new BorderLayout(10, 10));
        transparentPanel.setOpaque(false);
        transparentPanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        backgroundLabel.add(transparentPanel, BorderLayout.CENTER);

        // UI: Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchPanel.setOpaque(false);
        cityField = new JTextField(16);
        JButton getWeatherButton = new JButton("Get Weather");
        searchPanel.add(new JLabel("City:"));
        searchPanel.add(cityField);
        searchPanel.add(getWeatherButton);
        transparentPanel.add(searchPanel, BorderLayout.NORTH);

        // UI: Result Area
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setForeground(Color.WHITE);
        resultArea.setBackground(new Color(0, 0, 0, 150));
        resultArea.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        transparentPanel.add(scrollPane, BorderLayout.CENTER);

        // Button Action
        getWeatherButton.addActionListener(e -> {
            String city = cityField.getText().trim();
            if (!city.isEmpty()) {
                setResultText("Fetching weather for " + city + "...\n");
                new Thread(() -> {
                    String weatherInfo = getWeather(city);
                    setResultText(weatherInfo);
                }).start();
            } else {
                JOptionPane.showMessageDialog(frame, "Please enter a city name.");
            }
        });

        // Fetch for Major Cities
        new Thread(() -> {
            StringBuilder allWeather = new StringBuilder();
            for (String city : MAJOR_CITIES) {
                allWeather.append(getWeather(city)).append("\n\n");
            }
            setResultText(allWeather.toString());
        }).start();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void setResultText(String s) {
        SwingUtilities.invokeLater(() -> resultArea.setText(s));
    }

    private static String getWeather(String city) {
        try {
            String urlString = API_URL + city + "&appid=" + API_KEY + "&units=metric";
            HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return parseWeatherData(response.toString());
            } else {
                return "Error: Could not fetch weather for " + city +
                        " (HTTP " + responseCode + ")";
            }
        } catch (Exception e) {
            return "Error fetching weather for " + city + ": " + e.getMessage();
        }
    }

    private static String parseWeatherData(String json) {
        try {
            String cityName = extractValue(json, "\"name\":\"", "\"");
            String temperature = extractValue(json, "\"temp\":", ",");
            String humidity = extractValue(json, "\"humidity\":", ",");
            String description = extractValue(json, "\"description\":\"", "\"");

            return "City: " + cityName + "\n"
                 + "Temperature: " + temperature + " °C\n"
                 + "Humidity: " + humidity + " %\n"
                 + "Condition: " + description;
        } catch (Exception e) {
            return "Error: Failed to parse weather data.";
        }
    }

    private static String extractValue(String json, String startKey, String endKey) {
        int start = json.indexOf(startKey);
        if (start < 0) return "N/A";
        start += startKey.length();
        int end = json.indexOf(endKey, start);
        if (end < 0) return "N/A";
        return json.substring(start, end);
    }
}
