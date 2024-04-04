package org.example.demo1;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;


@WebServlet(name = "helloServlet", value = "/hello-servlet")
public class HelloServlet extends HttpServlet {
    private String message;

    public void init() {
        message = "Hello World!";
    }

    String url = "jdbc:mysql://localhost:3306/sales";
    String user = "root";
    String pass = "";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(url, user, pass)) {
                response.setContentType("text/html");
                PrintWriter out = response.getWriter();

                String commandRead = "select * from Sellers";
                Statement stat = connection.createStatement();
                ResultSet result = stat.executeQuery(commandRead);

                out.println("<html><body>");
                out.println("<h1>" + "Інформація про продавців" + "</h1>");

                out.println("<table>");
                out.println("<tr> <th> id </th> <th>name_seller</th> <th>contact_seller</th> <th>email_seller</th> </tr> ");
                while (result.next()) {
                    int id = result.getInt("id");
                    String name_seller = result.getString("name_seller");
                    String contact_seller = result.getString("contact_seller");
                    String email_seller = result.getString("email_seller");

                    out.println("<tr> <td> " + id + "</td> <td> " + name_seller + " </td> <td>" + contact_seller + "</td> <td>" + email_seller + "</td> </tr> ");
                }
                out.println("</table>");

                out.println("<h2>Додати новий рядок:</h2>");
                out.println("<form action='/hello-servlet' method='post'>");
                out.println("Ім'я продавця: <input type='text' name='name'><br>");
                out.println("Контакт продавця: <input type='text' name='contact'><br>");
                out.println("Email продавця: <input type='text' name='email'><br>");
                out.println("<input type='submit' value='Додати рядок у таблицю'>");
                out.println("</form>");

                out.println("<h2>Видалити рядок за id:</h2>");
                out.println("<form action='/hello-servlet' method='post'>");
                out.println("Id для видалення: <input type='number' name='deleteId'><br>");
                out.println("<input type='submit' value='Видалити рядок з таблиці'>");
                out.println("</form>");

                out.println("<h2>Редагувати рядок за id:</h2>");
                out.println("<form action='/hello-servlet' method='post'>");
                out.println("Id для редагування: <input type='number' name='editId'><br>");
                out.println("<input type='submit' value='Знайти рядок для редагування'>");
                out.println("</form>");

                out.println("</body></html>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("name");
        String contact = request.getParameter("contact");
        String email = request.getParameter("email");
        String deleteId = request.getParameter("deleteId");
        String editId = request.getParameter("editId");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(url, user, pass)) {
                // Додавання рядка у таблицю за заданими параметрами
                if (name != null && contact != null && email != null) {
                    String insertQuery = "INSERT INTO Sellers (name_seller, contact_seller, email_seller) VALUES (?, ?, ?)";
                    PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
                    preparedStatement.setString(1, name);
                    preparedStatement.setString(2, contact);
                    preparedStatement.setString(3, email);
                    int rowsInserted = preparedStatement.executeUpdate();

                    if (rowsInserted > 0) {
                        response.setContentType("text/html");
                        PrintWriter out = response.getWriter();
                        out.println("<html><body>");
                        out.println("<h3>Рядок успішно добавлений!</h3>");
                        out.println("</body></html>");
                    }
                }

                else if (deleteId != null) {
                    String deleteQuery = "DELETE FROM Sellers WHERE id = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery);
                    preparedStatement.setInt(1, Integer.parseInt(deleteId));
                    int rowsDeleted = preparedStatement.executeUpdate();

                    if (rowsDeleted > 0) {
                        response.setContentType("text/html");
                        PrintWriter out = response.getWriter();
                        out.println("<html><body>");
                        out.println("<h3>Користувач із вказаним id видалено!</h3>");
                        out.println("</body></html>");
                    }
                }

                else if (editId != null) {
                    String selectQuery = "SELECT * FROM Sellers WHERE id = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
                    preparedStatement.setInt(1, Integer.parseInt(editId));
                    ResultSet resultSet = preparedStatement.executeQuery();

                    response.setContentType("text/html");
                    PrintWriter out = response.getWriter();
                    out.println("<html><body>");
                    if (resultSet.next()) {
                        String name_seller = resultSet.getString("name_seller");
                        String contact_seller = resultSet.getString("contact_seller");
                        String email_seller = resultSet.getString("email_seller");

                        out.println("<h3>Редагувати рядок з id=" + editId + "</h3>");
                        out.println("<form action='/hello-servlet' method='post'>");
                        out.println("Ім'я продавця: <input type='text' name='editedName' value='" + name_seller + "'><br>");
                        out.println("Контакт продавця: <input type='text' name='editedContact' value='" + contact_seller + "'><br>");
                        out.println("Email продавця: <input type='text' name='editedEmail' value='" + email_seller + "'><br>");
                        out.println("<input type='hidden' name='editId' value='" + editId + "'>");
                        out.println("<input type='submit' value='Редагувати рядок'>");
                        out.println("</form>");
                    } else {
                        out.println("<h3>Користувач із вказаним id не знайдено!</h3>");
                    }
                    out.println("</body></html>");
                }

                else if (request.getParameter("editedName") != null && request.getParameter("editedContact") != null && request.getParameter("editedEmail") != null) {
                    String editedName = request.getParameter("editedName");
                    String editedContact = request.getParameter("editedContact");
                    String editedEmail = request.getParameter("editedEmail");
                    String editedId = request.getParameter("editId");

                    String updateQuery = "UPDATE Sellers SET name_seller = ?, contact_seller = ?, email_seller = ? WHERE id = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
                    preparedStatement.setString(1, editedName);
                    preparedStatement.setString(2, editedContact);
                    preparedStatement.setString(3, editedEmail);
                    preparedStatement.setInt(4, Integer.parseInt(editedId));
                    int rowsUpdated = preparedStatement.executeUpdate();

                    if (rowsUpdated > 0) {
                        response.setContentType("text/html");
                        PrintWriter out = response.getWriter();
                        out.println("<html><body>");
                        out.println("<h3>Рядок успішно відредаговано!</h3>");
                        out.println("</body></html>");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
    }
}
