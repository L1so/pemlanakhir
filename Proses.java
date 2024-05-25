package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.stream.IntStream;
import javax.swing.table.DefaultTableModel;

public class Proses extends JFrame {
    private JTextField nama, no_tel, alamat;
    private JComboBox tanggal, bulan, tahun;
    private JRadioButton tombolLaki, tombolPuan;
    private ButtonGroup tombolGender;

    private String[] tgl = IntStream.rangeClosed(1, 31).mapToObj(Integer::toString).toArray(String[]::new);

    private String[] bln = { "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember" };

    private String[] thn = IntStream.rangeClosed(1850, 2015).mapToObj(Integer::toString).toArray(String[]::new);

    public Proses() {
        setTitle("Form Data Diri");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel pan = new JPanel();
        pan.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pan.setLayout(new GridLayout(8, 2, 5, 5));

        pan.add(new JLabel("Nama Lengkap: "));
        nama = new JTextField();
        pan.add(nama);

        pan.add(new JLabel("Tanggal Lahir: "));
        JPanel dobPanel = new JPanel(new GridLayout(1, 3));
        tanggal = new JComboBox<>(tgl);
        dobPanel.add(tanggal);
        bulan = new JComboBox<>(bln);
        dobPanel.add(bulan);
        tahun = new JComboBox<>(thn);
        dobPanel.add(tahun);
        pan.add(dobPanel);

        pan.add(new JLabel("Jenis Kelamin: "));
        JPanel panelKelamin = new JPanel(new GridLayout(1, 2));
        tombolLaki = new JRadioButton("Laki-laki");
        tombolPuan = new JRadioButton("Perempuan");
        tombolGender = new ButtonGroup();
        tombolGender.add(tombolLaki);
        tombolGender.add(tombolPuan);
        panelKelamin.add(tombolLaki);
        panelKelamin.add(tombolPuan);
        pan.add(panelKelamin);

        pan.add(new JLabel("No Tel: "));
        no_tel = new JTextField();
        pan.add(no_tel);

        pan.add(new JLabel("Alamat: "));
        alamat = new JTextField();
        pan.add(alamat);

        JButton submit = new JButton("Submit");
        submit.addActionListener(this::handleSubmit);
        pan.add(submit);

        JButton tampil = new JButton("Tampilkan Data");
        tampil.addActionListener(this::tombolTampil);
        pan.add(tampil);

        add(pan);
        setVisible(true);
    }

    private void handleSubmit(ActionEvent e) {
        String jenis_kelamin = tombolLaki.isSelected() ? "Laki-laki" : (tombolPuan.isSelected() ? "Perempuan" : "");
        UIManager.put("OptionPane.okButtonText", "Kembali");
        UIManager.put("OptionPane.noButtonText", "Cancel");
        UIManager.put("OptionPane.yesButtonText", "Ya");
        if (nama.getText().isEmpty() || jenis_kelamin.isEmpty() ||
                no_tel.getText().isEmpty() || alamat.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua kolom harus terisi.", "Peringatan", JOptionPane.WARNING_MESSAGE);
        } else if (JOptionPane.showConfirmDialog(this, "Apakah anda yakin data yang Anda isi sudah benar?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            String tgl_lahir = tahun.getSelectedItem() + "-" + (bulan.getSelectedIndex() + 1) + "-" + tanggal.getSelectedItem();
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/data_java", "root", "123")) {
                String sql = "INSERT INTO data_diri (nama, tgl_lahir, gender, notel, alamat) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstm = conn.prepareStatement(sql)) {
                    pstm.setString(1, nama.getText());
                    pstm.setString(2, tgl_lahir);
                    pstm.setString(3, jenis_kelamin);
                    pstm.setString(4, no_tel.getText());
                    pstm.setString(5, alamat.getText());
                    pstm.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Data berhasil disimpan ke database.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                }
                tampilkanTabel(conn);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan data dikarenakan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void tombolTampil(ActionEvent e) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/data_java", "root", "123")) {
            tampilkanTabel(conn);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mengambil data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tampilkanTabel(Connection conn) {
        JFrame tableFrame = new JFrame("Data Diri");
        tableFrame.setSize(800, 400);
        tableFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] namaKolom = {"ID", "Nama", "Tanggal Lahir", "Gender", "No Telp", "Alamat"};
        DefaultTableModel tabel = new DefaultTableModel(namaKolom, 0);

        try (Statement stmt = conn.createStatement();
             ResultSet hasil = stmt.executeQuery("SELECT * FROM data_diri")) {

            while (hasil.next()) {
                int id = hasil.getInt("id");
                String nama = hasil.getString("nama");
                Date tgl_lahir = hasil.getDate("tgl_lahir");
                String gender = hasil.getString("gender");
                String notel = hasil.getString("notel");
                String alamat = hasil.getString("alamat");
                Object[] data = {id, nama, tgl_lahir, gender, notel, alamat};
                tabel.addRow(data);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JTable table = new JTable(tabel);
        JScrollPane scrollPane = new JScrollPane(table);
        tableFrame.add(scrollPane);

        tableFrame.setVisible(true);
    }
}
