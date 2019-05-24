import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

class Word {
    public String eng, vie;

    public Word(String Eng, String Vie){
        this.eng = Eng;
        this.vie = Vie;
    }
}

class wordUBS {
    int index;
    Word word;
    public wordUBS(int index, Word word){
        this.index = index;
        this.word = word;
    }
}

public class Dictionary extends JFrame{
    private JPanel panel = new JPanel();
    private JMenuBar jMenuBar = new JMenuBar();
    private JMenu menu = new JMenu("Menu");
    private JMenuItem add = new JMenuItem("Thêm từ");
    private JMenuItem delete = new JMenuItem("Xóa từ");
    private JMenuItem edit = new JMenuItem("Sửa từ");
    private JMenuItem showAll = new JMenuItem("Danh sách từ");
    private JMenuItem info = new JMenuItem("Thông tin");
    private JMenuItem instruction = new JMenuItem("Hướng dẫn");
    private JTextField engInput = new JTextField();
    private JLabel logo = new JLabel();
    private JLabel muiTen = new JLabel();
    private JLabel vieField = new JLabel();
    private DefaultListModel<Word> listE = new DefaultListModel<Word>();
    private JList<Word> englishList = new JList<Word>(listE);
    private JScrollPane jScrollPane = new JScrollPane(englishList);
    private JButton speak = new JButton();
    private JButton translate = new JButton("Dịch");
    private List<Word> data = new LinkedList<Word>();
    private static int pos = 0;
    private int editAtPos;
    private Stack<Stack<wordUBS>> unBackSpace = new Stack<Stack<wordUBS>>();
    private String preState = "";

    public Dictionary(){
        getDataFromFile();
        implementGui();
    }

    private void popUBS(){
        if (!unBackSpace.empty()){
            Stack<wordUBS> temporary = unBackSpace.pop();
            while (!temporary.empty()){
                wordUBS wordUBS = temporary.pop();
                listE.add(wordUBS.index, wordUBS.word);
            }
        }
    }

    private void getDataFromFile(){
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream("data.txt"), "UTF-8"));
            String str, strE, strV;
            while ((str=bufferedReader.readLine())!=null){
                strE = str.substring(0,str.indexOf('/'));
                strV = str.substring(str.indexOf('/')+1);
                data.add(new Word(strE,strV));
            }
            bufferedReader.close();
        }catch (Exception e){
            System.out.print(e.toString());
        }
    }

    private void exportToFile(){
        try{
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data.txt"), "UTF-8"));
            int n = data.size();
            for(int i=0; i<n; i++) {
                writer.write(data.get(i).eng + "/" + data.get(i).vie + "\n");
            }
            writer.close();
        }catch (Exception e){
            System.out.println(e.toString());
        }
    }

    private ListCellRenderer<? super String> getBorder() {
        return new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel listCellRendererComponent = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,cellHasFocus);
                listCellRendererComponent.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,Color.LIGHT_GRAY));
                return listCellRendererComponent;
            }
        };
    }

    private ListCellRenderer<? super Word> getEngOfWord() {
        return new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel listCellRendererComponent = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,cellHasFocus);
                Word w = (Word) value;
                listCellRendererComponent.setText(w.eng);
                listCellRendererComponent.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,Color.LIGHT_GRAY));
                return listCellRendererComponent;
            }
        };
    }

    private int compare(Word o1, Word o2) {
        int w1Length = o1.eng.length();
        int w2Length = o2.eng.length();
        int min = w2Length;
        if (w1Length < w2Length) min = w1Length;
        for(int i = 0; i < min; i++){
            if ((int)o1.eng.charAt(i) > (int)o2.eng.charAt(i)) return 1;
            else if ((int)o1.eng.charAt(i) < (int)o2.eng.charAt(i)) return -1;
        }
        if (w1Length > min) return 1;
        else if (w2Length > min) return -1;
        return 0;
    }

    private boolean isApartOf(String s1, String s2){
        int length1 = s1.length();
        int length2 = s2.length();
        int min = length1;
        if (length2 < min) min = length2;
        for(int i = 0; i < min; i++){
            if (s1.charAt(i) != s2.charAt(i)) return false;
        }
        return true;
    }

    private int differentAt(String s1, String s2){
        int length1 = s1.length();
        int length2 = s2.length();
        int min = length1;
        for(int i = 0; i < min; i++){
            if (s1.charAt(i) != s2.charAt(i)) return i;
        }
        return -1;
    }

    private void setListResultE(){
        for(Word i : data){
            listE.addElement(i);
        }
        int height = 0, numberOfElement = listE.getSize();
        if (numberOfElement == 1) height = 22;
        else if (numberOfElement == 2) height = 42;
        else if (numberOfElement == 3) height = 60;
        else if (numberOfElement == 4) height = 80;
        else if (numberOfElement >= 5) height = 100;
        jScrollPane.setSize(180, height);
        jScrollPane.setLocation(25,190);
        jScrollPane.setVisible(true);
    }

    private void search(String text){
        int textLength = text.length();
        for(int j = 0; j < textLength; j++){
            Stack<wordUBS> list = new Stack<wordUBS>();
            for(int i = 0; i < listE.size(); i++){
                if (listE.get(i).eng.length() < pos+j+1){
                    list.push(new wordUBS(i, listE.get(i)));
                    listE.remove(i);
                    i--;
                }else if (listE.get(i).eng.charAt(pos + j) != text.charAt(j)){
                    list.push(new wordUBS(i, listE.get(i)));
                    listE.remove(i);
                    i--;
                }
            }
            unBackSpace.add(list);
        }
        pos+=textLength;
        if (listE.size() == 1 && pos == listE.get(0).eng.length()){
            jScrollPane.setSize(180,0);
            englishList.setSelectedIndex(0);
            vieField.setText(" " + listE.get(0).vie);
        }else{
            int height = 0, numberOfElement = listE.getSize();
            if (numberOfElement == 1) height = 22;
            else if (numberOfElement == 2) height = 42;
            else if (numberOfElement == 3) height = 60;
            else if (numberOfElement == 4) height = 80;
            else if (numberOfElement >= 5) height = 100;
            jScrollPane.setSize(180, height);
            englishList.setSelectedIndex(0);
        }
    }



    public void implementGui(){

        //this frame
        this.setTitle("Từ điển Anh-Việt");
        this.setLocation(700,300);
        this.setSize(500,350);
        this.setResizable(false);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/image/icon.png")));
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getModifiers() == InputEvent.BUTTON1_MASK){
                    jScrollPane.setVisible(false);
                }
            }
        });

        //panel
        panel.setOpaque(true);
        panel.setBackground(Color.white);
        panel.setLayout(null);

        //thanh menu
        jMenuBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getModifiers() == InputEvent.BUTTON1_MASK){
                    jScrollPane.setVisible(false);
                }
            }
        });

        //menu
        menu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getModifiers() == InputEvent.BUTTON1_MASK){
                    jScrollPane.setVisible(false);
                }
            }
        });

        //xem tất cả từ
        showAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultListModel list = new DefaultListModel();
                if (data.size()>0){
                    for(Word i : data){
                        list.addElement(" "+i.eng + ": " + i.vie);
                    }
                    JList listSA = new JList<>(list);
                    listSA.setFont(new Font("Times New Roman", 0, 14));
                    listSA.setCellRenderer(getBorder());
                    JScrollPane Scroll = new JScrollPane(listSA);
                    JFrame contain = new JFrame("Danh sách các từ");
                    contain.add(Scroll);
                    contain.setVisible(true);
                    contain.setSize(300,400);
                    contain.setLocation(800,280);
                    contain.setResizable(false);
                }else{
                    JOptionPane.showMessageDialog(null,"Từ điển trống!\n Đảm bảo file data.txt có dữ liệu và cùng folder vời Từ Điển!", "Danh sách từ", JOptionPane.ERROR_MESSAGE);
                }}
        });

        //thêm từ
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTextField engTextFieldOfAdd = new JTextField();
                JTextField vieTextFieldOfAdd = new JTextField();

                JFrame frameOfAdd = new JFrame("Thêm từ");
                frameOfAdd.setVisible(true);
                frameOfAdd.setSize(300,210);
                frameOfAdd.setLocation(800,350);
                frameOfAdd.setResizable(false);

                JLabel eStatusLabelOfAdd = new JLabel("", SwingConstants.CENTER);
                eStatusLabelOfAdd.setFont(new Font("Times New Roman", 1, 12));
                eStatusLabelOfAdd.setSize(300,20);
                eStatusLabelOfAdd.setLocation(0,60);

                JLabel statusLabelOfAdd = new JLabel("", SwingConstants.CENTER);
                statusLabelOfAdd.setFont(new Font("Times New Roman", 1, 12));
                statusLabelOfAdd.setSize(300,20);
                statusLabelOfAdd.setLocation(0,115);

                JPanel panelOfAdd = new JPanel();
                panelOfAdd.setBackground(Color.LIGHT_GRAY);
                panelOfAdd.setLayout(null);

                engTextFieldOfAdd.setSize(150,25);
                engTextFieldOfAdd.setLocation(110, 30);
                engTextFieldOfAdd.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getModifiers() == InputEvent.BUTTON1_MASK){
                            eStatusLabelOfAdd.setText("");
                            statusLabelOfAdd.setText("");
                            vieTextFieldOfAdd.setEditable(true);
                        }
                    }
                });

                vieTextFieldOfAdd.setSize(150,25);
                vieTextFieldOfAdd.setLocation(110,85);
                vieTextFieldOfAdd.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getModifiers() == InputEvent.BUTTON1_MASK){
                            statusLabelOfAdd.setText("");
                            String eng = engTextFieldOfAdd.getText();
                            for(Word w : data){
                                if (w.eng.equals(eng)){
                                    vieTextFieldOfAdd.setEditable(false);
                                    eStatusLabelOfAdd.setForeground(Color.RED);
                                    eStatusLabelOfAdd.setText("Từ \""+engTextFieldOfAdd.getText()+"\" đã có trong Từ Điển!");
                                    break;
                                }
                            }
                        }
                    }
                });

                JLabel engLabelOfAdd = new JLabel("English:");
                engLabelOfAdd.setFont(new Font("Times New Roman", 0, 14));
                engLabelOfAdd.setSize(60,25);
                engLabelOfAdd.setLocation(40,30);

                JLabel vieLabelOfAdd = new JLabel("Tiếng Việt:");
                vieLabelOfAdd.setFont(new Font("Times New Roman", 0, 14));
                vieLabelOfAdd.setSize(63,25);
                vieLabelOfAdd.setLocation(40,85);

                JButton addButton = new JButton("Thêm");
                addButton.setFont(new Font("Times New Roman", 0, 14));
                addButton.setSize(70,27);
                addButton.setLocation(115,140);
                addButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String eng = engTextFieldOfAdd.getText();
                        String vie = vieTextFieldOfAdd.getText();

                        if (eng.equals("")){
                            statusLabelOfAdd.setForeground(Color.RED);
                            statusLabelOfAdd.setText("Bạn chưa nhập từ Tiếng Anh!");
                        }else if (vie.equals("")){
                            statusLabelOfAdd.setForeground(Color.RED);
                            statusLabelOfAdd.setText("Bạn chưa nhập từ Tiếng Việt!");
                        }else{
                            Word w = new Word(eng, vie);
                            //add vào file và data
                            int n = data.size();
                            if (data.size() == 0){
                                data.add(w);
                            }else if (compare(w,data.get(0)) == -1){
                                data.add(0,w);
                                exportToFile();
                                statusLabelOfAdd.setForeground(Color.BLUE);
                                statusLabelOfAdd.setText("Thêm thành công!");
                                engTextFieldOfAdd.setText("");
                                vieTextFieldOfAdd.setText("");
                            }else if (compare(w,data.get(n-1)) == 1){
                                data.add(n,w);
                                exportToFile();
                                statusLabelOfAdd.setForeground(Color.BLUE);
                                statusLabelOfAdd.setText("Thêm thành công!");
                                engTextFieldOfAdd.setText("");
                                vieTextFieldOfAdd.setText("");
                            } else for(int j = 0; j < n-1; j++){
                                if (compare(w,data.get(j)) == 1 && compare(w,data.get(j+1)) == -1){
                                    data.add(j+1, w);
                                    exportToFile();
                                    statusLabelOfAdd.setForeground(Color.BLUE);
                                    statusLabelOfAdd.setText("Thêm thành công!");
                                    engTextFieldOfAdd.setText("");
                                    vieTextFieldOfAdd.setText("");
                                    break;
                                }
                            }
                            //add vào list tìm kiếm
                            if (isApartOf(preState, eng)){
                                int m = listE.size();
                                if (m > 0){
                                    if (compare(w, listE.get(0)) == -1){
                                        listE.add(0,w);
                                    }else if (compare(w, listE.get(m-1)) == 1){
                                        listE.add(m,w);
                                    }else for(int j = 0; j < m-1; j++){
                                        if (compare(w, listE.get(j)) == 1 && compare(w, listE.get(j+1)) == -1){
                                            listE.add(j+1, w);
                                            break;
                                        }
                                    }
                                }
                            }else{
                                int preStateLength = preState.length();
                                int index = 0;
                                for(int i = 0; i < preStateLength; i++){
                                    if (preState.charAt(i) != eng.charAt(i)){
                                        index = i;
                                        break;
                                    }
                                }
                                Stack<wordUBS> stack = unBackSpace.get(index);
                                int stackSize = stack.size();
                                if (stackSize==0){
                                    stack.add(new wordUBS(0,w));
                                }else if (compare(w, stack.get(0).word) == -1){
                                    stack.add(0,new wordUBS(stack.get(0).index, w));
                                }else if (compare(w, stack.get(stackSize-1).word) == 1){
                                    stack.add(stackSize,new wordUBS(stack.get(stackSize-1).index, w));
                                }else for(int j = 0; j < stackSize-1; j++){
                                    if (compare(w, stack.get(j).word) == 1 && compare(w, stack.get(j+1).word) == -1){
                                        if (stack.get(j).word.eng.charAt(index)<=w.eng.charAt(index)){
                                            stack.add(j+1, new wordUBS(stack.get(j).index, w));
                                        }else if (stack.get(j+1).word.eng.charAt(index)>=w.eng.charAt(index)){
                                            stack.add(j+1, new wordUBS(stack.get(j+1).index, w));
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                });
                panelOfAdd.add(engTextFieldOfAdd);
                panelOfAdd.add(vieTextFieldOfAdd);
                panelOfAdd.add(engLabelOfAdd);
                panelOfAdd.add(vieLabelOfAdd);
                panelOfAdd.add(eStatusLabelOfAdd);
                panelOfAdd.add(statusLabelOfAdd);
                panelOfAdd.add(addButton);
                frameOfAdd.add(panelOfAdd);
            }
        });

        //xóa từ
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JLabel engLableOfDelete = new JLabel("Từ cần xóa:");
                engLableOfDelete.setFont(new Font("Times New Roman", 0, 14));
                engLableOfDelete.setSize(68,25);
                engLableOfDelete.setLocation(15,20);

                JPanel panelOfDelete = new JPanel();
                panelOfDelete.setBackground(Color.LIGHT_GRAY);
                panelOfDelete.setLayout(null);

                JLabel statusLabelOfDelete = new JLabel("", SwingConstants.CENTER);
                statusLabelOfDelete.setFont(new Font("Times New Roman", 1, 12));
                statusLabelOfDelete.setSize(250,20);
                statusLabelOfDelete.setLocation(0,50);

                JTextField engTextFieldOfDelete = new JTextField();
                engTextFieldOfDelete.setFont(new Font("Times New Roman", 0, 14));
                engTextFieldOfDelete.setSize(135,25);
                engTextFieldOfDelete.setLocation(90,20);
                engTextFieldOfDelete.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getModifiers() == InputEvent.BUTTON1_MASK){
                            statusLabelOfDelete.setText("");
                        }
                    }
                });

                JButton deleteButton = new JButton("Xóa");
                deleteButton.setFont(new Font("Times New Roman", 0, 14));
                deleteButton.setSize(60,25);
                deleteButton.setLocation(95,74);
                deleteButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String eng = engTextFieldOfDelete.getText();
                        boolean isDeleted = false;
                        if (eng.equals("")){
                            statusLabelOfDelete.setForeground(Color.RED);
                            statusLabelOfDelete.setText("Mời bạn nhập từ cần xóa!");
                        }else {
                            for(Word i : data){
                                if (i.eng.equals(eng)){
                                    data.remove(i);
                                    if (listE.size() > 0) {
                                        if (!listE.removeElement(i)){
                                            int stackStackSize = unBackSpace.size();
                                            for(int j = 0; j < stackStackSize; j++){
                                                int stackSize = unBackSpace.get(j).size();
                                                for(int k = 0; k < stackSize; k++){
                                                    if (unBackSpace.get(j).get(k).word.eng.equals(eng)) {
                                                        unBackSpace.get(j).remove(k);
                                                        for(int l=0; l < j; l++){
                                                            for(int m = 0; m < unBackSpace.get(l).size(); m++){
                                                              if ((int)unBackSpace.get(l).get(m).word.eng.charAt(l) > (int)eng.charAt(l)){
                                                                  unBackSpace.get(l).get(m).index--;
                                                              }
                                                            }
                                                        }
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    isDeleted = true;
                                    exportToFile();
                                    break;
                                }
                            }
                            if (isDeleted){
                                engTextFieldOfDelete.setText("");
                                statusLabelOfDelete.setForeground(Color.BLUE);
                                statusLabelOfDelete.setText("Đã xóa từ \"" + eng + "\"!");
                            }else{
                                engTextFieldOfDelete.setText("");
                                statusLabelOfDelete.setForeground(Color.RED);
                                statusLabelOfDelete.setText("Không có từ \"" + eng + "\" trong từ điển!");
                            }
                        }
                    }
                });

                JFrame frameOfDelete = new JFrame("Xoá từ");
                frameOfDelete.setVisible(true);
                frameOfDelete.setSize(250,135);
                frameOfDelete.setLocation(800,350);
                frameOfDelete.setResizable(false);

                panelOfDelete.add(engLableOfDelete);
                panelOfDelete.add(engTextFieldOfDelete);
                panelOfDelete.add(statusLabelOfDelete);
                panelOfDelete.add(deleteButton);
                frameOfDelete.add(panelOfDelete);
            }
        });

        //sửa từ
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTextField engTextFieldOfEdit = new JTextField();
                JTextField vieTextFieldOfEdit = new JTextField();

                JFrame frameOfEdit = new JFrame("Sửa nghĩa của từ Tiếng Anh");
                frameOfEdit.setVisible(true);
                frameOfEdit.setSize(320,210);
                frameOfEdit.setLocation(800,350);
                frameOfEdit.setResizable(false);

                JLabel eStatusLabelOfEdit = new JLabel("", SwingConstants.CENTER);
                eStatusLabelOfEdit.setFont(new Font("Times New Roman", 1, 12));
                eStatusLabelOfEdit.setSize(320,20);
                eStatusLabelOfEdit.setLocation(0,60);

                JLabel statusLabelOfEdit = new JLabel("", SwingConstants.CENTER);
                statusLabelOfEdit.setFont(new Font("Times New Roman", 1, 12));
                statusLabelOfEdit.setSize(320,20);
                statusLabelOfEdit.setLocation(0,115);

                JPanel panelOfEdit = new JPanel();
                panelOfEdit.setBackground(Color.LIGHT_GRAY);
                panelOfEdit.setLayout(null);



                engTextFieldOfEdit.setSize(175,25);
                engTextFieldOfEdit.setLocation(110, 30);
                engTextFieldOfEdit.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getModifiers() == InputEvent.BUTTON1_MASK){
                            eStatusLabelOfEdit.setText("");
                            statusLabelOfEdit.setText("");
                            vieTextFieldOfEdit.setEditable(true);
                        }
                    }
                });

                vieTextFieldOfEdit.setSize(175,25);
                vieTextFieldOfEdit.setLocation(110,85);
                vieTextFieldOfEdit.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getModifiers() == InputEvent.BUTTON1_MASK){
                            statusLabelOfEdit.setText("");
                            boolean isWordExisting = false;
                            String eng = engTextFieldOfEdit.getText();
                            for(Word w : data){
                                if (w.eng.equals(eng)){
                                    editAtPos = data.indexOf(w);
                                    isWordExisting = true;
                                    break;
                                }
                            }
                            if (!isWordExisting){
                                vieTextFieldOfEdit.setEditable(false);
                                eStatusLabelOfEdit.setForeground(Color.RED);
                                eStatusLabelOfEdit.setText("Không có từ \""+eng+"\" trong Từ Điển!");
                            }
                        }
                    }
                });

                JLabel engLabelOfEdit = new JLabel("Từ cần sửa:");
                engLabelOfEdit.setFont(new Font("Times New Roman", 0, 14));
                engLabelOfEdit.setSize(68,25);
                engLabelOfEdit.setLocation(35,30);

                JLabel vieLabelOfEdit = new JLabel("Nghĩa mới:");
                vieLabelOfEdit.setFont(new Font("Times New Roman", 0, 14));
                vieLabelOfEdit.setSize(63,25);
                vieLabelOfEdit.setLocation(35,85);

                JButton editButton = new JButton("Sửa");
                editButton.setFont(new Font("Times New Roman", 0, 14));
                editButton.setSize(70,27);
                editButton.setLocation(125,140);
                editButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String eng = engTextFieldOfEdit.getText();
                        String vie = vieTextFieldOfEdit.getText();
                        if (eng.equals("")){
                            statusLabelOfEdit.setForeground(Color.RED);
                            statusLabelOfEdit.setText("Bạn chưa nhập từ Tiếng Anh!");
                        }else if (vie.equals("")){
                            statusLabelOfEdit.setForeground(Color.RED);
                            statusLabelOfEdit.setText("Bạn chưa nhập nghĩa Tiếng Việt mới!");
                        }else{
                            data.get(editAtPos).vie = vie;
                            exportToFile();
                            engTextFieldOfEdit.setText(null);
                            vieTextFieldOfEdit.setText(null);
                            statusLabelOfEdit.setForeground(Color.BLUE);
                            statusLabelOfEdit.setText("Đã sửa nghĩa của \"" + eng + "\" thành \"" + vie + "\"!");
                        }
                        boolean edited = false;
                        if (listE.size() > 0){
                            for(int i = 0; i < listE.size(); i++){
                                if (listE.get(i).eng.equals(eng)){
                                    listE.get(i).vie = vie;
                                    edited = true;
                                }
                            }
                            if (!edited){
                                int stackStackSize = unBackSpace.size();
                                for(int j = 0; j < stackStackSize; j++){
                                    int stackSize = unBackSpace.get(j).size();
                                    for(int k = 0; k < stackSize; k++){
                                        if (unBackSpace.get(j).get(k).word.eng.equals(eng)) {
                                            unBackSpace.get(j).get(k).word.vie = vie;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                });

                panelOfEdit.add(engTextFieldOfEdit);
                panelOfEdit.add(vieTextFieldOfEdit);
                panelOfEdit.add(engLabelOfEdit);
                panelOfEdit.add(vieLabelOfEdit);
                panelOfEdit.add(eStatusLabelOfEdit);
                panelOfEdit.add(statusLabelOfEdit);
                panelOfEdit.add(editButton);
                frameOfEdit.add(panelOfEdit);
            }
        });

        //hướng dẫn
        instruction.setMaximumSize(new Dimension(77,45));
        instruction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jScrollPane.setVisible(false);
                JOptionPane.showMessageDialog(null, "1. Dịch (khung cửa sổ chính):\n" +
                        "       - Nhập từ Tiếng Anh cần tra vào Text Box bên trái rồi ấn \"Dịch\" hoặc Enter.\n" +
                        "       - Kết quả trả về khi ấn \"Dịch\" hoặc Enter ưu tiên từ đầu tiên trong danh sách kết quả.\n" +
                        "       - Khi nhập xong từ và danh sách kết quả ít hơn 2 phần tử, nghĩa sẽ tự hiện ra.\n" +
                        "       - Có thế chọn từ trong danh sách bằng DoubleClick.\n" +
                        "       - Có thế chèn thêm 1 hoặc nhiều ký tự vào vị trí bất kỳ.\n" +
                        "       - Có thế xóa 1 hoặc nhiều kí tự bất kì cùng 1 lúc ở ô tìm kiếm.\n" +
                        "       - Có thế Copy-Paste 1 từ hoặc 1 phần của từ vào ô tìm kiếm.\n" +
                        "2. Danh sách từ (Menu => Danh sách từ): Hiện tất cả các từ trong từ điển.\n" +
                        "3. Thêm từ (Menu => Thêm từ): Thêm 1 từ chưa có trong từ điển.\n" +
                        "4. Sửa từ (Menu => Sửa từ): Sửa 1 từ đã có trong từ điển.\n" +
                        "5. Xóa từ (Meunu => Xóa từ): Xóa 1 từ không ưng ý trong từ điển.", "Hướng dẫn", JOptionPane.DEFAULT_OPTION);
            }
        });

        //thông tin
        info.setMaximumSize(new Dimension(65,45));
        info.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jScrollPane.setVisible(false);
                JOptionPane.showMessageDialog(null, "Tác giả:\nNguyễn Trọng Rưỡng \nLã Quốc Việt \n\nĐH Công nghệ - ĐHQGHN.", "Thông tin", JOptionPane.INFORMATION_MESSAGE);
            }
        });



        //logo
        logo.setIcon(new ImageIcon(getClass().getResource("/image/logo.png")));
        logo.setSize(416,130);
        logo.setLocation(42,0);

        //vùng nhập tiếng Anh
        engInput.setFont(new Font("Times New Roman", 0, 18));
        engInput.setSize(180,40);
        engInput.setLocation(25,150);
        engInput.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (listE.size() == 0 && engInput.getText().equals("")){
                    setListResultE();
                    vieField.setText("");
                }else{
                    jScrollPane.setVisible(true);
                }
            }
        });
        engInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE){
                    String floState = engInput.getText();
                    if (isApartOf(floState,preState)){
                        int length = engInput.getText().length();
                        for(int i = 0; i < pos - length; i++){
                            popUBS();
                        }
                        pos = length;
                        if (listE.size()==1 && pos == listE.get(0).eng.length()){
                            jScrollPane.setSize(180,0);
                            vieField.setText(" " + listE.get(0).vie);
                        }else{
                            vieField.setText("");
                            int height = 0, numberOfElement = listE.getSize();
                            if (numberOfElement == 1) height = 22;
                            else if (numberOfElement == 2) height = 42;
                            else if (numberOfElement == 3) height = 60;
                            else if (numberOfElement == 4) height = 80;
                            else if (numberOfElement >= 5) height = 100;
                            jScrollPane.setSize(180, height);
                            englishList.setSelectedIndex(0);
                        }
                        preState = floState;
                    }else{
                        while (!unBackSpace.empty()){
                            popUBS();
                        }
                        pos = 0;
                        search(floState);
                        preState = floState;
                    }
                }else if (e.getKeyCode() == KeyEvent.VK_ENTER){
                    if (listE.size()>0){
                        engInput.setText(englishList.getSelectedValue().eng);
                        search(englishList.getSelectedValue().eng.substring(pos));
                        vieField.setText(" " + englishList.getSelectedValue().vie);
                        jScrollPane.setSize(180,0);
                    }
                }else if (e.getKeyCode() == KeyEvent.VK_UP){
                    int currentIndex = englishList.getSelectedIndex();
                    if (currentIndex > 0) {
                        englishList.setSelectedIndex(currentIndex-1);
                        englishList.ensureIndexIsVisible(currentIndex);
                    }
                }else if (e.getKeyCode() == KeyEvent.VK_DOWN){
                    int currentIndex = englishList.getSelectedIndex();
                    if (currentIndex < listE.size()-1) {
                        englishList.setSelectedIndex(currentIndex+1);
                        englishList.ensureIndexIsVisible(currentIndex);
                    }
                }else if (e.getKeyCode() != KeyEvent.VK_CONTROL || e.getKeyCode() != KeyEvent.VK_LEFT || e.getKeyCode() != KeyEvent.VK_RIGHT){
                    vieField.setText("");
                    if (isApartOf(preState, engInput.getText())){
                        String text = engInput.getText().substring(pos);
                        preState += text;
                        search(text);
                    }else{
                        int i = differentAt(preState, engInput.getText());
                        while (unBackSpace.size() > i){
                            popUBS();
                        }
                        pos = i;
                        preState = engInput.getText();
                        search(preState.substring(i));
                    }
                }
            }
        });

        //icon mũi tên
        muiTen.setIcon(new ImageIcon(getClass().getResource("/image/muiTen.jpg")));
        muiTen.setSize(65,40);
        muiTen.setLocation(217,150);

        //vùng nghĩa tiếng Việt
        vieField.setFont(new Font("Times New Roman", 0, 18));
        vieField.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
        vieField.setSize(180,40);
        vieField.setLocation(295,150);

        //nút phát âm
        speak.setIcon(new ImageIcon(getClass().getResource("/image/speakIcon.png")));
        speak.setSize(30,30);
        speak.setLocation(235,200);
        speak.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    FileInputStream fis = new FileInputStream("sound/" + engInput.getText() + ".mp3");
                    Player player = new Player(fis);
                    player.play();
                } catch (FileNotFoundException ex) {
                    if (vieField.getText().equals("")){
                        try {
                            FileInputStream fis = new FileInputStream("sound/0001.mp3");
                            Player player = new Player(fis);
                            player.play();
                        } catch (FileNotFoundException ex1) {
                            System.out.println(ex.toString());
                        } catch (JavaLayerException ex2) {
                            System.out.println(ex.toString());
                        }
                    }else{
                        try {
                            FileInputStream fis = new FileInputStream("sound/0002.mp3");
                            Player player = new Player(fis);
                            player.play();
                        } catch (FileNotFoundException ex1) {
                            System.out.println(ex.toString());
                        } catch (JavaLayerException ex2) {
                            System.out.println(ex.toString());
                        }
                    }
                } catch (JavaLayerException ex3) {
                    System.out.println(ex3.toString());
                }
            }
        });

        //nút dịch
        translate.setFont(new Font("Times New Roman", 1, 18));
        translate.setSize(70,40);
        translate.setLocation(215,240);
        translate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                engInput.setText(englishList.getSelectedValue().eng);
                search(englishList.getSelectedValue().eng.substring(pos));
                vieField.setText(" "+englishList.getSelectedValue().vie);
                jScrollPane.setSize(180,0);
            }
        });

        //danh sách kết quả
        englishList.setCellRenderer(getEngOfWord());
        englishList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        englishList.setFont(new Font("Times New Roman", 0, 14));

        englishList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e){
                if (e.getKeyCode() == KeyEvent.VK_ENTER){
                    engInput.setText(englishList.getSelectedValue().eng);
                    search(englishList.getSelectedValue().eng.substring(pos));
                    vieField.setText(" " + englishList.getSelectedValue().vie);
                    jScrollPane.setSize(180,0);
                }
            }
        });

        englishList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2){
                    engInput.setText(englishList.getSelectedValue().eng);
                    search(englishList.getSelectedValue().eng.substring(pos));
                    vieField.setText(" " + englishList.getSelectedValue().vie);
                    jScrollPane.setSize(180,0);
                }
            }
        });

        //nơi thêm element
        menu.add(showAll);
        menu.add(add);
        menu.add(edit);
        menu.add(delete);
        jMenuBar.add(menu);
        jMenuBar.add(instruction);
        jMenuBar.add(info);
        panel.add(translate);
        panel.add(logo);
        panel.add(engInput);
        panel.add(muiTen);
        panel.add(vieField);
        panel.add(speak);
        panel.add(jScrollPane);
        this.add(panel);
        this.setJMenuBar(jMenuBar);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable(){
            public void run() {
                new Dictionary().setVisible(true);
            }
        } );
    }
}