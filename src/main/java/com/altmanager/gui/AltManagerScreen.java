package com.altmanager.gui;

import com.altmanager.data.Profile;
import com.altmanager.data.ProfileManager;
import com.altmanager.session.SessionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

import java.awt.Color;
import java.util.List;

/**
 * Pantalla de gestión de perfiles ("Alt Manager").
 * Permite añadir, editar, usar (cambiar de sesión sin reiniciar) y eliminar perfiles.
 * Los datos persisten en disco a través de {@link ProfileManager}.
 */
public class AltManagerScreen extends Screen {

	private static final int ROW_HEIGHT = 24;
	private static final int LIST_TOP = 90;

	private final Screen parent;
	private EditBox nameField;
	private Button confirmButton;
	private Button importButton;

	/** Si no es null, estamos editando este perfil en vez de añadir uno nuevo. */
	private String editingProfileId = null;

	/** Posición Y de la fila del perfil seleccionado, para pintar el resaltado detrás de los botones. -1 = ninguno. */
	private int selectedRowY = -1;

	public AltManagerScreen(Screen parent) {
		super(Component.literal("Alt Manager"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		ProfileManager.get().load();

		int centerX = this.width / 2;

		this.nameField = new EditBox(this.font, centerX - 150, 35, 220, 20,
				Component.literal("Nombre del perfil"));
		this.nameField.setMaxLength(400); // suficiente para pegar una lista larga separada por comas

		this.confirmButton = Button.builder(Component.literal("Añadir"), button -> onConfirm())
				.bounds(centerX + 75, 35, 75, 20)
				.build();

		Button importButtonInstance = Button.builder(Component.literal("Importar lista (separada por comas)"), button -> onImportList())
				.bounds(centerX - 150, 58, 300, 20)
				.build();
		this.importButton = importButtonInstance;

		rebuildList();
	}

	private void onConfirm() {
		String name = this.nameField.getValue();
		if (this.editingProfileId != null) {
			ProfileManager.get().renameProfile(this.editingProfileId, name);
			this.editingProfileId = null;
			this.confirmButton.setMessage(Component.literal("Añadir"));
		} else {
			ProfileManager.get().addProfile(name);
		}
		this.nameField.setValue("");
		rebuildList();
	}

	/**
	 * Toma el texto del campo (nombres separados por coma, punto y coma, o salto de línea)
	 * y crea un perfil por cada nombre no vacío.
	 */
	private void onImportList() {
		String raw = this.nameField.getValue();
		String[] parts = raw.split("[,;\\n]+");
		int added = 0;
		for (String part : parts) {
			String name = part.trim();
			if (!name.isEmpty()) {
				ProfileManager.get().addProfile(name);
				added++;
			}
		}
		if (added > 0) {
			this.nameField.setValue("");
		}
		rebuildList();
	}

	private void onEdit(Profile profile) {
		this.editingProfileId = profile.getId();
		this.nameField.setValue(profile.getName());
		this.confirmButton.setMessage(Component.literal("Guardar"));
	}

	/** Cambia la sesión activa del cliente al nombre del perfil, sin reiniciar el juego. */
	private void onUse(Profile profile) {
		SessionUtil.switchSession(profile.getName());
		ProfileManager.get().selectProfile(profile.getId());
		rebuildList();
	}

	/**
	 * Reconstruye los widgets de la lista de perfiles.
	 * Se llama cada vez que la lista cambia (añadir, editar, usar, eliminar).
	 */
	private void rebuildList() {
		this.clearWidgets();
		this.addRenderableWidget(this.nameField);
		this.addRenderableWidget(this.confirmButton);
		this.addRenderableWidget(this.importButton);
		this.addRenderableWidget(Button.builder(Component.literal("Volver"), button -> {
					if (this.minecraft != null) {
						this.minecraft.setScreen(this.parent);
					}
				})
				.bounds(this.width / 2 - 100, this.height - 30, 200, 20)
				.build());

		List<Profile> profiles = ProfileManager.get().getProfiles();
		String selectedId = ProfileManager.get().getSelectedProfileId();

		int centerX = this.width / 2;
		int y = LIST_TOP;
		this.selectedRowY = -1;

		for (Profile profile : profiles) {
			boolean isSelected = profile.getId().equals(selectedId);
			if (isSelected) {
				this.selectedRowY = y;
			}

			Component nameComponent = isSelected
					? Component.literal("★ " + profile.getName())
							.withStyle(ChatFormatting.BOLD)
							.withStyle(style -> style.withColor(TextColor.fromRgb(0xFFD700))) // dorado
					: Component.literal(profile.getName());

			this.addRenderableWidget(Button.builder(nameComponent, button -> onUse(profile))
					.bounds(centerX - 150, y, 110, 20)
					.build());

			this.addRenderableWidget(Button.builder(Component.literal("Usar"), button -> onUse(profile))
					.bounds(centerX - 35, y, 60, 20)
					.build());

			this.addRenderableWidget(Button.builder(Component.literal("Editar"), button -> onEdit(profile))
					.bounds(centerX + 30, y, 60, 20)
					.build());

			this.addRenderableWidget(Button.builder(Component.literal("Eliminar"), button -> {
						ProfileManager.get().removeProfile(profile.getId());
						if (profile.getId().equals(this.editingProfileId)) {
							this.editingProfileId = null;
							this.nameField.setValue("");
							this.confirmButton.setMessage(Component.literal("Añadir"));
						}
						rebuildList();
					})
					.bounds(centerX + 95, y, 75, 20)
					.build());

			y += ROW_HEIGHT;
		}
	}

	/** Calcula un color que va rotando en el tiempo, para un efecto de texto "arcoíris". */
	private static int rainbowColor(int offset) {
		float time = System.currentTimeMillis() / 12f;
		float hue = ((time + offset * 18f) % 360f) / 360f;
		return Color.HSBtoRGB(hue, 0.6f, 1.0f) & 0xFFFFFF;
	}

	/** Dibuja un texto letra por letra, cada una con un color distinto que rota con el tiempo. */
	private void drawRainbowText(GuiGraphics context, String text, int startX, int y) {
		int x = startX;
		for (int i = 0; i < text.length(); i++) {
			String letter = String.valueOf(text.charAt(i));
			context.drawString(this.font, letter, x, y, rainbowColor(i));
			x += this.font.width(letter);
		}
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		// Resaltado detrás de la fila del perfil seleccionado (se dibuja antes que los botones).
		if (this.selectedRowY >= 0) {
			int centerX = this.width / 2;
			context.fill(centerX - 155, this.selectedRowY - 2, centerX + 175, this.selectedRowY + 22, 0x552ECC71);
			context.fill(centerX - 155, this.selectedRowY - 2, centerX - 153, this.selectedRowY + 22, 0xFF2ECC71);
		}

		super.render(context, mouseX, mouseY, delta);

		context.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);

		String label = "Sesión activa: " + SessionUtil.getActiveUsername();
		int textWidth = this.font.width(label);
		drawRainbowText(context, label, this.width / 2 - textWidth / 2, 22);

		List<Profile> profiles = ProfileManager.get().getProfiles();
		if (profiles.isEmpty()) {
			context.drawCenteredString(this.font,
					Component.literal("No hay perfiles guardados todavía."),
					this.width / 2, LIST_TOP + 10, 0xAAAAAA);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		if (this.minecraft != null) {
			this.minecraft.setScreen(this.parent);
		}
	}
}
