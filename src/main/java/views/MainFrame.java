package views;

import models.testmodule.ModulesPermitTableModel;
import models.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.AppProperties;
import utils.Frames;
import utils.UtilityMethods;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Головне вікно програми.
 * @author Oleksii Smolenskyi
 */
public class MainFrame extends JFrame {
	private static final Logger LOGGER = LogManager.getLogger(MainFrame.class.getName());
	private volatile ModulePermitFrame modulePermitFrame;
	private static SignInFrame signInFrame;
	private static MainFrame instance;
	private ModulesPermitTableModel modulesPermitModel;
	private User currentUser;
	private JButton buttonTestPinMeasured;
	private JButton buttonPermit;
	private JButton buttonFastCheck;

	// приватний конструктор...створення об'Єкта класу відбувається через виклик метода getInstance().
	private MainFrame() {
		//init();
		this.setLayout(new FlowLayout());
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// виклик форми входу
		callSignInFrame();
		// init components
		initPermitButton();
		initMeasuredButton();
		initProgramsFastCheckButton();
		// перевіряєм чи властивості програми не коцнуті
		setEnableButtons(AppProperties.isPropertiesOK());
		initMainMenu();
		this.pack();
		this.revalidate();
		this.setBounds(Frames.getBoundsForCenteringFrame(this));
		this.setVisible(true);
	}

	private void init() {
		try {
			if(!UtilityMethods.getMainPath().contains("1_Lager\\TM_freigabe\\libs")) {
				System.exit(0);
			}
		} catch (IOException e) {
			System.exit(0);
		}
	}

	// Ініціалізує головне меню
	private void initMainMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu tools = new JMenu("Tools");
		menuBar.add(tools);
		this.setJMenuBar(menuBar);
		// створюєм пункт меню виклику вікна редагування користувачів
		JMenuItem editUsers = new JMenuItem("Користувачі");
		editUsers.addActionListener(e -> new EditUsersFrame(this));
		// перевіряєм чи користувач має права для доступу до редагування користувачів
		if(currentUser.hasAccessToUsers())
			tools.add(editUsers);
		// створюєм пункт меню виклику вікна налаштувань програми
		JMenuItem preferense = new JMenuItem("Налаштування");
		preferense.addActionListener(e -> new AppPropertiesDialog(this));
		// перевіряєм чи користувач має права доступу до редагування налаштувань програми
		if(currentUser.hasAccessToPreferencesApp())
			tools.add(preferense);
	}

	// Ініціалізує кнопку виклику форми вимірів голок
	private void initMeasuredButton() {
		final String imgForMeasuringButton = "pinsMeasured.png";
		buttonTestPinMeasured = new JButton(); //"Виміри голок");
		ImageIcon icon = new ImageIcon(MainFrame.class.getClassLoader().getResource(imgForMeasuringButton));
		buttonTestPinMeasured.setIcon(icon);
		buttonTestPinMeasured.addActionListener(e -> {
			Thread thread = new Thread(() -> {
				try {
					new PinsMeasuredFrame();
				} catch (IOException ex) {
					Frames.showErrorMessage(MainFrame.this, ex.getMessage());
				}
			});
			thread.setDaemon(true);
			thread.start();
		});
		this.getContentPane().add(buttonTestPinMeasured);
	}

	// Ініціалізує кнопку виклику форми допусків модулів
	private void initPermitButton() {
		final String imgForPermitButton = "modulePermits.png";
		buttonPermit = new JButton(); //"Допуски тест. модулів");
		ImageIcon icon = new ImageIcon(MainFrame.class.getClassLoader().getResource(imgForPermitButton));
		buttonPermit.setIcon(icon);
		buttonPermit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread(() -> {
					if (modulePermitFrame == null || !modulePermitFrame.isShowing()) {
						try {
							modulesPermitModel = new ModulesPermitTableModel();
							modulePermitFrame = new ModulePermitFrame(modulesPermitModel);
							modulesPermitModel.addObserver(modulePermitFrame);
						} catch (IOException ioe) {
							Frames.showErrorMessage(MainFrame.this, ioe.getMessage());
						}
					} else {
						modulePermitFrame.requestFocus();
					}
				});
				thread.setDaemon(true);
				thread.start();
			}
		});
		this.getContentPane().add(buttonPermit);
	}

	// Ініціалізує кнопку виклику форми допусків модулів
	private void initProgramsFastCheckButton() {
		final String imgForPermitButton = "programsFastCheck.png";
		buttonFastCheck = new JButton(); //"Допуски тест. модулів");
		ImageIcon icon = new ImageIcon(MainFrame.class.getClassLoader().getResource(imgForPermitButton));
		buttonFastCheck.setIcon(icon);
		buttonFastCheck.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread(() -> {
					new ProgramsFastCheckDialog();
				});
				thread.setDaemon(true);
				thread.start();
			}
		});
		this.getContentPane().add(buttonFastCheck);
	}

	// повертає екземпляр класу, клас реалізується як сінглетон
	public static synchronized MainFrame getInstance() {
		if (instance == null)
			instance = new MainFrame();
		return instance;
	}

	// точка входу в програму
	public static void main(String[] args) {
		if (args.length == 0) {
			try {
				//http://blog.codejava.net/nam/trick-for-passing-vm-options-when-launching-jar-file/
				// re-launch the app itselft with VM option passed
				// самий робочий і практичний трюк який найшов
				Runtime.getRuntime().exec(new String[]{"java", "-Xmx1024m", "-jar", "assistantengineer-1.0.jar", "test"});
			} catch (IOException ioe) {
				Frames.showErrorMessage(null, ioe.getMessage());
				LOGGER.error(ioe.getMessage());
			}
			System.exit(0);
		}
		MainFrame.getInstance();
		LOGGER.info("Program started.");
	}

	// метод виклику форми входу
	public void callSignInFrame() {
		// форма входу
		signInFrame = new SignInFrame();
		while (signInFrame.isVisible()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		currentUser = signInFrame.getCurrentUser();
		signInFrame.dispose();
		this.setTitle(currentUser.getTabelNr());
		this.setVisible(true);
	}

	// задає доступність кнопок функціоналу
	public void setEnableButtons(boolean enable) {
		if(!currentUser.hasAccessToPermitsOfModules()) {
			buttonPermit.setEnabled(false);
		} else {
			buttonPermit.setEnabled(enable);
		}
		if(!currentUser.hasAccessToPinsMeasuringResults()) {
			buttonTestPinMeasured.setEnabled(false);
		} else {
			buttonTestPinMeasured.setEnabled(enable);
		}
		buttonFastCheck.setEnabled(true);
	}

	// Повертає залогованого користувача.
	public User getCurrentUser() {
		return currentUser;
	}
}