package views;

import models.User;
import utils.Frames;
import utils.UsersManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Вікно логування користувача.
 */
class SignInFrame extends JFrame {
	private JTextField jFieldUserNr; // поле для вводу табельного номера користувача
	private JTextField jFieldUserPass; // поле для вводу паролю користувача
	private JButton loginButton; // кнопка входу
	// розміри вікна
	private final int width = 250;
	private final int height = 120;
	private User currentUser;

	public SignInFrame() {
		super("Sign in");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// задаєм розміри вікна
		this.setBounds(0,0, width, height);
		this.setBounds(Frames.getBoundsForCenteringFrame(this));
		this.setResizable(false);
		// задаєм лайаут фрейму
		this.setLayout(new FlowLayout());
		initComponents();
	}

	// метод ініціалізації елементів на формі
	private void initComponents() {
		// поле для табельного номеру
		jFieldUserNr = new JTextField("", 20);
		// створюєм текст підказку в полі вводу тального номеру користувача
		jFieldUserNr.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if(jFieldUserNr.getText().equals("S00000")) {
					jFieldUserNr.setText("");
				}
			}
			@Override
			public void focusLost(FocusEvent e) {
				if(jFieldUserNr.getText().equals("")) {
					jFieldUserNr.setText("S00000");
				}
			}
		});
		// ініціалізуєм поле для пароля
		jFieldUserPass = new JPasswordField("", 20);
		jFieldUserPass.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					login();
				}
			}
		});
		// ініціалізація кнопки логування
		loginButton = new JButton("Login");
		loginButton.addActionListener(e -> login());
		// додаємо все на фрейм
		this.getContentPane().add(jFieldUserNr);
		this.getContentPane().add(jFieldUserPass);
		this.getContentPane().add(loginButton);
		this.revalidate();
		// показуєм фрейм
		setVisible(true);
	}

	// метод перевірки введених знаень, чи є такий користувач
	private void login() {
		User user;
		// отримуєм користувача з існуючих записів згідно введених значень таб. номеру і паролю
		user = UsersManager.verifyUser(jFieldUserNr.getText(), jFieldUserPass.getText());
		// якщо користувач існує згідно введених даних
		if (user != null) {
			currentUser = user;
			this.setVisible(false);
		} else { // якщо користувач НЕ існує згідно введених даних
			Frames.showErrorMessage(this, "Хибний логін або пароль!");
		}
	}

	public User getCurrentUser() {
		return currentUser;
	}
}