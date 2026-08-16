package com.example.gyeonjutravel.domain.stamp.service;

import com.example.gyeonjutravel.domain.stamp.entity.StampAlbum;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class StampAlbumImageRenderer {

    private static final int WIDTH = 360;
    private static final int HEIGHT = 640;
    private static final Color BACKGROUND = new Color(248, 245, 239);
    private static final Color SURFACE = new Color(240, 236, 229);
    private static final Color TEXT = new Color(42, 37, 33);
    private static final Color MUTED = new Color(111, 101, 92);
    private static final Color ACCENT = new Color(238, 125, 70);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M월 d일");

    public byte[] render(StampAlbum album, String stampName) {
        BufferedImage canvas = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            applyQualityHints(graphics);
            graphics.setColor(BACKGROUND);
            graphics.fillRect(0, 0, WIDTH, HEIGHT);

            drawHeader(graphics, album);
            drawPhotos(graphics, album);
            drawFootprintPanel(graphics, album);
            drawMapPanel(graphics, stampName);
            drawActionPreview(graphics);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(canvas, "png", outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("스탬프 앨범 이미지를 생성하지 못했습니다.", exception);
        } finally {
            graphics.dispose();
        }
    }

    private void drawHeader(Graphics2D graphics, StampAlbum album) {
        graphics.setColor(TEXT);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 24));
        graphics.drawString("오늘의 경주", 18, 38);

        graphics.setColor(MUTED);
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 12));
        graphics.drawString(album.getPet().getName() + "와 함께한 하루", 18, 60);

        graphics.setFont(new Font("SansSerif", Font.PLAIN, 11));
        graphics.drawString(album.getSchedule().getTravelDate().format(DATE_FORMATTER), 286, 38);
    }

    private void drawPhotos(Graphics2D graphics, StampAlbum album) {
        List<String> photoUrls = album.getPhotos().stream()
                .map(photo -> photo.getImageUrl())
                .toList();
        drawRotatedPhoto(graphics, photoUrls.isEmpty() ? null : photoUrls.get(0), 24, 84, 146, 116, -8, true);
        drawRotatedPhoto(graphics, photoUrls.size() < 2 ? null : photoUrls.get(1), 158, 124, 150, 116, 5, false);
        drawProfile(graphics, album.getPet().getProfileImageUrl(), 282, 214, 58);
    }

    private void drawFootprintPanel(Graphics2D graphics, StampAlbum album) {
        roundRect(graphics, 14, 294, 332, 60, 12, SURFACE);
        graphics.setColor(MUTED);
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 13));
        graphics.drawString(album.getPet().getName() + "의 발자국 지도", 40, 330);

        graphics.setColor(ACCENT);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 13));
        String footprint = album.getFootprintCount() + " 개";
        graphics.drawString(footprint, 280, 330);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 16));
        graphics.drawString("♣", 324, 330);
    }

    private void drawMapPanel(Graphics2D graphics, String stampName) {
        roundRect(graphics, 14, 376, 332, 178, 10, new Color(222, 218, 211));
        graphics.setColor(new Color(206, 202, 195));
        for (int x = 26; x < 330; x += 38) {
            graphics.drawLine(x, 384, x + 40, 548);
        }
        graphics.setColor(new Color(194, 190, 184));
        graphics.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.drawLine(66, 522, 124, 470);
        graphics.drawLine(124, 470, 140, 424);
        graphics.drawLine(140, 424, 204, 412);

        graphics.setColor(new Color(234, 126, 72, 150));
        for (int index = 0; index < 18; index++) {
            int x = 82 + (index * 19) % 86;
            int y = 410 + (index * 29) % 112;
            graphics.fillOval(x, y, 6, 6);
        }

        graphics.setColor(ACCENT);
        graphics.fillOval(128, 454, 28, 28);
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 14));
        centerText(graphics, "1", 142, 474);

        graphics.setColor(new Color(255, 252, 246));
        graphics.fillOval(284, 478, 58, 58);
        graphics.setColor(new Color(226, 198, 158));
        graphics.setStroke(new BasicStroke(2));
        graphics.drawOval(288, 482, 50, 50);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 10));
        graphics.setColor(new Color(88, 68, 48));
        centerText(graphics, stampName, 313, 512);
    }

    private void drawActionPreview(Graphics2D graphics) {
        roundRect(graphics, 22, 570, 152, 48, 8, SURFACE);
        roundRect(graphics, 188, 570, 152, 48, 8, SURFACE);
        graphics.setColor(MUTED);
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 14));
        centerText(graphics, "이미지로 저장", 98, 600);
        centerText(graphics, "SNS 공유", 264, 600);
    }

    private void drawRotatedPhoto(
            Graphics2D graphics,
            String imageUrl,
            int x,
            int y,
            int width,
            int height,
            double angle,
            boolean highlighted
    ) {
        AffineTransform originalTransform = graphics.getTransform();
        graphics.rotate(Math.toRadians(angle), x + width / 2.0, y + height / 2.0);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(x, y, width, height);
        graphics.setColor(highlighted ? new Color(28, 145, 252) : Color.WHITE);
        graphics.setStroke(new BasicStroke(highlighted ? 2 : 1));
        graphics.drawRect(x, y, width, height);

        BufferedImage photo = readImage(imageUrl);
        if (photo == null) {
            graphics.setColor(new Color(198, 218, 226));
            graphics.fillRect(x + 12, y + 18, width - 24, height - 38);
        } else {
            drawCoverImage(graphics, photo, x + 12, y + 18, width - 24, height - 38, null);
        }
        graphics.setTransform(originalTransform);
    }

    private void drawProfile(Graphics2D graphics, String imageUrl, int x, int y, int size) {
        Shape clip = new Ellipse2D.Double(x, y, size, size);
        BufferedImage profile = readImage(imageUrl);
        if (profile == null) {
            graphics.setColor(new Color(174, 137, 78));
            graphics.fill(clip);
            graphics.setColor(Color.WHITE);
            graphics.setFont(new Font("SansSerif", Font.BOLD, 20));
            centerText(graphics, "PET", x + size / 2, y + size / 2 + 7);
            return;
        }
        drawCoverImage(graphics, profile, x, y, size, size, clip);
    }

    private void drawCoverImage(Graphics2D graphics, BufferedImage image, int x, int y, int width, int height, Shape clip) {
        Shape originalClip = graphics.getClip();
        if (clip != null) {
            graphics.setClip(clip);
        } else {
            graphics.setClip(new RoundRectangle2D.Double(x, y, width, height, 4, 4));
        }
        double scale = Math.max(width / (double) image.getWidth(), height / (double) image.getHeight());
        int scaledWidth = (int) Math.ceil(image.getWidth() * scale);
        int scaledHeight = (int) Math.ceil(image.getHeight() * scale);
        int drawX = x + (width - scaledWidth) / 2;
        int drawY = y + (height - scaledHeight) / 2;
        graphics.drawImage(image, drawX, drawY, scaledWidth, scaledHeight, null);
        graphics.setClip(originalClip);
    }

    private BufferedImage readImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        try {
            return ImageIO.read(URI.create(imageUrl).toURL());
        } catch (Exception ignored) {
            return null;
        }
    }

    private void roundRect(Graphics2D graphics, int x, int y, int width, int height, int radius, Color color) {
        graphics.setColor(color);
        graphics.fillRoundRect(x, y, width, height, radius, radius);
    }

    private void centerText(Graphics2D graphics, String text, int centerX, int baselineY) {
        FontMetrics metrics = graphics.getFontMetrics();
        graphics.drawString(text, centerX - metrics.stringWidth(text) / 2, baselineY);
    }

    private void applyQualityHints(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }
}
