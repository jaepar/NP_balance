package client.screen;

import common.Game;
import common.User;

import javax.swing.*;
import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

//LoginPage
public class GameScreen1 extends GameScreen {
	public GameScreen1(ObjectOutputStream out, ObjectInputStream in, String gameScreenTitle, User thisUser,
					   HashMap<String, Game> gameList, HashMap<String, User> userList) {
		super(out, in, gameScreenTitle, thisUser, gameList, userList);
		System.out.println("생성한게임:"
				+ thisUser.getCreatedGameIds());
		System.out.println("참여한게임:"
				+ thisUser.getPlayedGameIds());
	}

	@Override
	public void showScreen() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 500);
		frame.setLayout(new BorderLayout(10, 10));

		// Create the main panel and set layout
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		// Add title label
		JLabel titleLabel = new JLabel("실시간 밸런스 게임");
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
		mainPanel.add(titleLabel);

		// Add spacing
		mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

		// Add ID input field
		JLabel idLabel = new JLabel("아이디 입력");
		idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		idLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
		JTextField idField = new JTextField(20);
		idField.setMaximumSize(new Dimension(300, 40));

		mainPanel.add(idLabel);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		mainPanel.add(idField);

		// Add spacing
		mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

		// Add password input field
		JLabel passwordLabel = new JLabel("비밀번호 입력");
		passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		passwordLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
		JPasswordField passwordField = new JPasswordField(20);
		passwordField.setMaximumSize(new Dimension(300, 40));

		mainPanel.add(passwordLabel);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		mainPanel.add(passwordField);

		// Add spacing
		mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));

		// Add button panel for "유저 등록" and "게임 입장"
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));

		JButton registerButton = new JButton("유저 등록");
		JButton enterGameButton = new JButton("게임 입장");

		registerButton.setPreferredSize(new Dimension(120, 40));
		enterGameButton.setPreferredSize(new Dimension(120, 40));

		buttonPanel.add(registerButton);
		buttonPanel.add(enterGameButton);

		mainPanel.add(buttonPanel);

		// Center the main panel in the frame
		JPanel wrapperPanel = new JPanel(new GridBagLayout());
		wrapperPanel.add(mainPanel);

		frame.add(wrapperPanel);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		// 이벤트 처리
		registerButton.addActionListener(e -> handleUserRegistration(idField.getText(), new String(passwordField.getPassword())));
		enterGameButton.addActionListener(e -> handleLogin(idField.getText(), new String(passwordField.getPassword())));
	}

	private void handleUserRegistration(String id, String password) {
		try {
			synchronized (_lock) {
				out.writeObject("REGISTER");
				out.writeObject(id);
				out.writeObject(password);
				out.flush();

				String response = (String) in.readObject();
				if ("REGISTER_SUCCESS".equals(response)) {
					JOptionPane.showMessageDialog(frame, "등록 성공!");
				} else if ("REGISTER_FAIL".equals(response)) {
					JOptionPane.showMessageDialog(frame, "등록 실패: 이미 존재하는 아이디.");
				} else {
					JOptionPane.showMessageDialog(frame, "알 수 없는 오류가 발생했습니다.");
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "서버와의 통신 중 오류가 발생했습니다.");
			e.printStackTrace();
		}
	}

	private void handleLogin(String id, String password) {
		try {
			synchronized (_lock) {
				out.writeObject("LOGIN");
				out.writeObject(id);
				out.writeObject(password);
				out.flush();

				String response = (String) in.readObject();
				System.out.println("Received response: " + response);
				if ("LOGIN_SUCCESS".equals(response)) {
					JOptionPane.showMessageDialog(frame, "로그인 성공!");
					User serverUser = (User) in.readObject(); // 서버에서 받은 데이터
					System.out.println("Received User object: " + serverUser);

					System.out.println("Played Game IDs: " + serverUser.getPlayedGameIds());

					_thisUser.loadFrom(serverUser); // 기존 객체에 데이터 반영

					// 다음 화면으로 이동
					GameScreen2 gameScreen2 = new GameScreen2(out, in, "게임 목록", _thisUser, _gameList, _userList);
					gameScreen2.showScreen();
					closeScreen();
				} else if ("LOGIN_FAIL".equals(response)) {
					JOptionPane.showMessageDialog(frame, "로그인 실패: 아이디 또는 비밀번호 확인.");
				} else {
					JOptionPane.showMessageDialog(frame, "알 수 없는 오류가 발생했습니다.");
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "서버와의 통신 중 오류가 발생했습니다.");
			e.printStackTrace();
		}
	}

	private JPanel createLabeledField(String label, JTextField field) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JLabel fieldLabel = new JLabel(label);
		fieldLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(fieldLabel);
		panel.add(field);
		field.setMaximumSize(new Dimension(300, 30));
		return panel;
	}
}