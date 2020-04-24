package org.ice1000.bililive.danmaku;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.NumberFormat;

public class DanmakuWindow {
	private JPanel component;
	private JButton updateRoom;
	private JFormattedTextField roomId;
	private JPanel content;

	public DanmakuWindow() {
		updateRoom.setDefaultCapable(true);
	}

	public @NotNull JPanel getComponent() {
		return component;
	}

	public @NotNull JButton getUpdateRoom() {
		return updateRoom;
	}

	public @NotNull JFormattedTextField getRoomId() {
		return roomId;
	}

	public void setContent(@NotNull JComponent component) {
		content.removeAll();
		content.add(component, BorderLayout.CENTER);
	}

	private void createUIComponents() {
		var formatter = new NumberFormatter(NumberFormat.getIntegerInstance());
		roomId = new JFormattedTextField(formatter);
	}
}
