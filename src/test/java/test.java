import java.io.*;

class test {
    public static void main(String[] args) {
        int[] n = new int[0];
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("objects.dat"));
            n = (int[]) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        n[0] = 12000;
        try {
//создание цепи потоков с потоком вывода объекта в конце
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("objects.dat"));

//java.util.* был импортирован для использования класса Date
            out.writeObject(n);

            out.close();
        } catch (IOException e) {


        }


        System.out.println();
    }
}