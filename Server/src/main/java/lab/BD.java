package lab;


import lab.BasicClasses.*;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import org.postgresql.Driver;


/**
 * Класс - база данных, позволяет проводить операции с базой данных. Без него коллекция не будет работать.
 * @autor Шахов Кирилл Андреевич P3132
 * @version 1.1
 */
public class BD {
    /** Поле, которое хранит путь до файла с базой данных */
    static Connection connection = null;
    /** Колекция, которая используется для представления данных в работающей программе. */
    private static List<MusicBand> data = Collections.synchronizedList(new ArrayList<>());
    private static List<User> users = Collections.synchronizedList(new ArrayList<>());
    /** Лист, который хранит историю введённых команд. */
    private static ArrayList<String> history = new ArrayList<>();

    public static boolean reverse = false;

    private static BD bd = null;
    public BD(String db_url, String user, String pass) {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager
                    .getConnection(db_url, user, pass);
            System.out.println("Connection Successful");
            if (createTable()){
                System.out.println("Creation Successful");
                if (load()){
                    System.out.println("Load Successful");
                }
                else{
                    System.out.println("Load Failed");
                    System.exit(0);
                }
            }
            else{
                System.out.println("Creation Failed");
                System.exit(0);
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Connection Failed");
            e.printStackTrace();
            System.exit(0);
        }
    }

    /** Метод, позволяет получить id для нового объекта.
     *
     * @return возвращает int ID
     * */
    public static long giveID(){
        boolean is = false;
        synchronized(data) {
            for (int result = 0; result < data.size(); result++) {
                for (MusicBand m : data) {
                    if (m.getID() == result) {
                        is = true;
                    }
                }
                if (is) {
                    is = false;
                } else {
                    return result;
                }
            }
        }
        return data.size();
    }

    public static int giveID_User(){

        try (ResultSet generatedKeys = connection.createStatement().getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
            else {
                throw new SQLException("Creating user failed, no ID obtained.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    /** Метод, позволяющий подметить какой-либо объект по ID.
     * @param id ID объекта, который мы хотим поменять.
     * @param musicBand объект.
     *
     * @return возвращает успешность выполнения метода. true - успех, false - исключение
     * */
    public static boolean update(MusicBand musicBand, Long id){
        try{
            String name = musicBand.getName();
            Date creation_Date = Date.valueOf(musicBand.getCreationDate().toLocalDate());
            String numberOfParticipants = String.valueOf(musicBand.getNumberOfParticipants());
            String description = musicBand.getDescription();
            Date establishment_Date;
            if (musicBand.getEstablishmentDate() != null) {
                establishment_Date = Date.valueOf(musicBand.getEstablishmentDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
            else{
                establishment_Date = null;
            }
            String genre = String.valueOf(musicBand.getGenre());
            String album_name = musicBand.getBestAlbum().getName();
            String album_tracks = String.valueOf(musicBand.getBestAlbum().getTracks());
            String album_lenght = String.valueOf(musicBand.getBestAlbum().getLength());
            String album_sales = String.valueOf(musicBand.getBestAlbum().getSales());


            String sql = "UPDATE DATA_BD "
                    + "SET "
                    + "NAME = ?, "
                    + "X = ?, "
                    + "Y = ?, "
                    + "CREATION_DATE = ?,"
                    + "NUMBER_OF_PARTICIPANTS = ?,"
                    + "DESCRIPTION = ?,"
                    + "ESTABLISHMENT_DATE = ?,"
                    + "GENRE = ?,"
                    + "ALBUM_NAME = ?,"
                    + "ALBUM_TRACKS = ?,"
                    + "ALBUM_LENGTH = ?,"
                    + "ALBUM_SALES = ? "
                    + "WHERE ID=?;";

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, name);
            statement.setFloat(2, Float.parseFloat(String.valueOf(musicBand.getCoordinates().getX())));
            statement.setFloat(3, musicBand.getCoordinates().getY());
            statement.setDate(4, creation_Date);
            statement.setInt(5, Integer.parseInt(numberOfParticipants));
            statement.setString(6, description);
            statement.setDate(7, establishment_Date);
            statement.setString(8, genre);
            statement.setString(9, album_name);
            statement.setInt(10, Integer.parseInt(album_tracks));
            statement.setInt(11, Integer.parseInt(album_lenght));
            statement.setInt(12, Integer.parseInt(album_sales));
            statement.setLong(13, id);

            int numberOfUpdatedRows = statement.executeUpdate();
            //connection.commit();
            synchronized(data) {
                data.set(Math.toIntExact(id), musicBand);
            }
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    /** Метод позволяет сохранить коллекцию в файл, название файла указывалось присоздании объекта.
     *
     * @return возвращает успешность выполнения метода. true - успех, false - исключение
     * */
    public static boolean save(){
        //сохранять нечего
        return true;
    }

    public static String removeGreater(MusicBand musicBand) {
        int f = 0;
        synchronized(data) {
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).getNumberOfParticipants() > musicBand.getNumberOfParticipants()) {
                    //noinspection SuspiciousListRemoveInLoop
                    data.remove(i);
                    f++;
                }
            }
        }
        return String.format("Удалено элементов: %s", f);
    }



    /** Метод, позволяет загрузить коллекцию из файла.
     *
     * @return возвращает успешность выполнения метода. true - успех, false - исключение
     * */
    private boolean load(){
        String SQL;
        ResultSet resultSet;
        try {
            SQL = "SELECT * FROM DATA_BD";
            resultSet = connection.createStatement().executeQuery(SQL);
            while (resultSet.next()) {
                Long id = resultSet.getLong("ID");
                String name = resultSet.getString("NAME");
                double x = resultSet.getDouble("X");
                Float y = resultSet.getFloat("Y");
                Date creation_date = resultSet.getDate("CREATION_DATE");
                LocalDateTime ldt = Instant.ofEpochMilli( creation_date.getTime() )
                        .atZone( ZoneId.systemDefault() )
                        .toLocalDateTime();
                //LocalDateTime ldt = LocalDateTime.ofInstant(Instant.from(creation_date.toLocalDate()), ZoneId.systemDefault());
                //Date out = (Date) Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());

                int number_of_participants = resultSet.getInt("NUMBER_OF_PARTICIPANTS");
                String description = resultSet.getString("DESCRIPTION");
                Date establishmentDate = resultSet.getDate("ESTABLISHMENT_DATE");
                String genre = resultSet.getString("GENRE");
                String album_name = resultSet.getString("ALBUM_NAME");
                int album_tracks = resultSet.getInt("ALBUM_TRACKS");
                int album_length = resultSet.getInt("ALBUM_LENGTH");
                int album_sales = resultSet.getInt("ALBUM_SALES");
                String user_creator = resultSet.getString("USER_CREATOR");
                synchronized(data) {
                    BD.data.add(new MusicBand(id, name, new Coordinates(x, y), ldt, number_of_participants, description, establishmentDate, MusicGenre.valueOf(genre), new Album(album_name, album_tracks, album_length, album_sales), user_creator));
                }
            }
            SQL = "SELECT * FROM USERS";
            resultSet = connection.createStatement().executeQuery(SQL);
            while (resultSet.next()) {
                String name = resultSet.getString("NAME");
                String pass = resultSet.getString("PASS");
                synchronized(users) {
                    users.add(new User(name, pass));
                }
            }
            BD.sort();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    /** Метод, позволяет добавить объект в коллекцию.
     *
     * @param musicBand Объект.
     *
     * @return возвращает успешность выполнения метода. true - успех, false - исключение
     * */
    public static boolean add(MusicBand musicBand){
        try {
            Statement stmt = connection.createStatement();
            String id = String.valueOf(giveID());
            String name = musicBand.getName();
            String x = String.valueOf(musicBand.getCoordinates().getX());
            String y = String.valueOf(musicBand.getCoordinates().getY());
            System.out.println(musicBand.getCreationDate());
            Date creation_Date = Date.valueOf(musicBand.getCreationDate().toLocalDate());
            String numberOfParticipants = String.valueOf(musicBand.getNumberOfParticipants());
            String description = musicBand.getDescription();
            Date establishment_Date;
            if (musicBand.getEstablishmentDate() != null) {
                establishment_Date = Date.valueOf(musicBand.getEstablishmentDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
            else{
                establishment_Date = null;
            }
            String genre = String.valueOf(musicBand.getGenre());
            String album_name = musicBand.getBestAlbum().getName();
            String album_tracks = String.valueOf(musicBand.getBestAlbum().getTracks());
            String album_lenght = String.valueOf(musicBand.getBestAlbum().getLength());
            String album_sales = String.valueOf(musicBand.getBestAlbum().getSales());
            String user_creator =  musicBand.getUser_creator();
            //String sql = "INSERT INTO DATAS (ID,NAME,X,Y,CREATION_DATE,NUMBER_OF_PARTICIPANTS,DESCRIPTION,ESTABLISHMENT_DATE,GENRE,ALBUM_NAME,ALBUM_TRACKS,ALBUM_LENGTH,ALBUM_SALES,USER_CREATOR) VALUES ({},'{}',{},{},{},{},'{}',{},'{}','{}',{},{},{},'{}');".format(id, name, x, y, creationDate, numberOfParticipants, description, establishmentDate, genre, album_name, album_tracks, album_lenght, album_sales, user_creator);

            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO DATA_BD (ID,NAME,X,Y,CREATION_DATE,NUMBER_OF_PARTICIPANTS,DESCRIPTION,ESTABLISHMENT_DATE,GENRE,ALBUM_NAME,ALBUM_TRACKS,ALBUM_LENGTH,ALBUM_SALES,USER_CREATOR) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
            preparedStatement.setInt(1, Integer.parseInt(id));
            preparedStatement.setString(2, name);
            preparedStatement.setFloat(3, Float.parseFloat(String.valueOf(musicBand.getCoordinates().getX())));
            preparedStatement.setFloat(4, musicBand.getCoordinates().getY());
            preparedStatement.setDate(5, creation_Date);
            preparedStatement.setInt(6, Integer.parseInt(numberOfParticipants));
            preparedStatement.setString(7, description);
            preparedStatement.setDate(8, establishment_Date);
            preparedStatement.setString(9, genre);
            preparedStatement.setString(10, album_name);
            preparedStatement.setInt(11, Integer.parseInt(album_tracks));
            preparedStatement.setInt(12, Integer.parseInt(album_lenght));
            preparedStatement.setInt(13, Integer.parseInt(album_sales));
            preparedStatement.setString(14, user_creator);
            preparedStatement.executeUpdate();

            synchronized(data) {
                data.add(musicBand);
            }
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public static boolean addUser(User user){
        try {
            Statement stmt = connection.createStatement();
            String name = user.getName();
            String pass = sha1(user.getPass());
            String sql = String.format("INSERT INTO USERS (NAME,PASS) VALUES ('%s', '%s');", name, pass);

            stmt.executeUpdate(sql);
            synchronized(users) {
                BD.users.add(new User(user.getName(), sha1(user.getPass())));
            }
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    /** Метод, возвращает Историю вводимых команд
     *
     * @return ArrayList<String> - История
     * */
    public ArrayList<String> getHistory() { return history; }
    /** Метод, позволяет удалять объекты из коллекции по ID.
     * ВАЖНО: ID в коллекции начинаються с 1, а не с 0.
     * @param id ID файла, который хотим удалить.
     *
     * @return возвращает успешность выполнения метода. true - успех, false - исключение.
     * */
    synchronized public static boolean remove(Long id) {
        try {
            Statement stmt = connection.createStatement();
            String sql = String.format("DELETE from DATA_BD where ID=%d;", id);
            stmt.executeUpdate(sql);
            data.removeIf(m -> m.getID() == id);
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    synchronized public static boolean remove_by_description(String des, String name) {
        try {
            Statement stmt = connection.createStatement();
            String sql = String.format("DELETE from DATA_BD where DESCRIPTION=%d;", des, name);
            stmt.executeUpdate(sql);
            data.removeIf(m -> m.getDescription().equals(des) & m.getUser_creator().equals(name));
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    /** Метод, позволяет очищать коллекцию.
     *
     * @return возвращает успешность выполнения метода. true - успех, false - исключение.
     * */
    public static boolean clean(){
        try {
            synchronized(data) {
                data = new ArrayList<>();
            }
            return true;
        }
        catch (Exception e){
            return false;
        }
    }
    /** Метод, позволяет получить дату создания файла.
     *
     * @return возвращает String - дата создания.
     * */
    public static String getCreateTime(){
        try {
            //return data.get(0).getCreateTime().toString();
            //BasicFileAttributes attr = Files.readAttributes(Paths.get(file_path), BasicFileAttributes.class);
            //return attr.creationTime().toString();
            return LocalDateTime.now().toString();
        }
        catch (Exception e){
            return "В коллекции нет элементов.";
        }
    }
    /** Метод, позволяет получить объект по его ID.
     * ВАЖНО: ID в коллекции начинаються с 1, а не с 0.
     *
     * @param id ID объекта.
     *
     * @return Объект MusicBand.
     * */
    public static MusicBand get(Long id){
        synchronized(data) {
            return data.get(Math.toIntExact(id));
        }
    }
    /** Метод, позволяет получить количество элементов в коллекции.
     *
     * @return int - колличество элементов.
     * */
    public static int size(){
        synchronized(data) {
            return data.size();
        }
    }
    /** Метод, позволяет записать команду в историю.
     *
     * @param command Команда, которую надо записать.
     * */
    public void log(String command) { history.add(command); }
    /** Метод, позволяет отсортировать массив по текущему методу сортировки.*/
    public static void sort(){
        synchronized(data) {
            if (!BD.reverse) {
                data.sort(Comparator.comparingLong(MusicBand::getID));
            } else {
                data.sort((player2, player1) -> Long.compare(player1.getID(), player2.getID()));
            }
        }
    }

    public static boolean checkExist(Long groupId) {
        synchronized(data) {
            for (MusicBand musicBand : data) {
                if (musicBand.getID() == groupId) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String removeLower(MusicBand musicBand) {
        int f = 0;
        synchronized(data) {
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).getNumberOfParticipants() < musicBand.getNumberOfParticipants()) {
                    //noinspection SuspiciousListRemoveInLoop
                    data.remove(i);
                    f++;
                }
            }
        }
        return String.format("Удалено %s элементов", f);
    }

    public static String sha1(String input) {
        String sha1 = null;
        try {
            MessageDigest msdDigest = MessageDigest.getInstance("SHA-1");
            msdDigest.update(input.getBytes(StandardCharsets.UTF_8), 0, input.length());
            sha1 = DatatypeConverter.printHexBinary(msdDigest.digest());
        } catch (NoSuchAlgorithmException ignored) {}
        return sha1;
    }
    private boolean createTable(){
        try {
            Statement stmt;
            stmt = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS USERS" +
                    "(NAME           TEXT    NOT NULL, " +
                    " PASS            TEXT     NOT NULL)";
            stmt.executeUpdate(sql);
            sql = "CREATE TABLE IF NOT EXISTS DATA_BD" +
                    "(ID INT PRIMARY KEY     NOT NULL," +
                    " NAME                   TEXT    NOT NULL, " +
                    " X                      FLOAT (24)    NOT NULL," +
                    " Y                      FLOAT(24)    NOT NULL," +
                    " CREATION_DATE          DATE    NOT NULL ," +
                    " NUMBER_OF_PARTICIPANTS INT NOT NULL," +
                    " DESCRIPTION            TEXT     NOT NULL," +
                    " ESTABLISHMENT_DATE     DATE     ," +
                    " GENRE                  TEXT     NOT NULL," +
                    " ALBUM_NAME             TEXT     NOT NULL," +
                    " ALBUM_TRACKS           INT     NOT NULL," +
                    " ALBUM_LENGTH           INT     NOT NULL," +
                    " ALBUM_SALES            INT     NOT NULL," +
                    " USER_CREATOR           TEXT     NOT NULL)";
            stmt.executeUpdate(sql);
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static int authUser(String name, String pass){
        User result = null;
        synchronized(users) {
            for (User user : users) {
                if (user.getName().equals(name)) {
                    result = user;
                    break;
                }
            }
        }
        if(result != null){
            if(result.getPass().equals(sha1(pass))){
                System.out.println("Пользователь авторизовался");
                return 1;//успешная авторизация
            }
            else {
                System.out.println("Пользователь не смог авторизоваться");
                return -1;//Неправильный пароль
            }
        }
        else {
            System.out.println("Пользователь не найден");
            return 0;// Пользователь не найден
        }
    }

    public static int registerUser(String name, String pass){
        User result = null;
        synchronized(users) {
            for (User user : users) {
                if (user.getName().equals(name)) {
                    result = user;
                    break;
                }
            }
        }
        if(result != null){
            System.out.println("Имя пользователя занято");
            return -1;
        }
        else {
            synchronized(users) {
                users.add(new User(name, sha1(pass)));
            }
            System.out.println("Пользователь зарегистрирован");
            return 0;// Пользователь зарегистрирован
        }
    }
    public static boolean checkPass(String name, String pass){
        User result = null;
        synchronized(users) {
            for(User user : users) {
                if (user.getName().equals(name)) {
                    result = user;
                    break;
                }
            }
        }
        if(result != null){
            //Неправильный пароль
            return result.getPass().equals(sha1(pass));//успешная авторизация
        }
        return false;
    }

    public static ArrayList<MusicBand> getData() {
        synchronized(data) {
            ArrayList<MusicBand> musicBands = new ArrayList<>(data);
            return musicBands;
        }
    }
}
