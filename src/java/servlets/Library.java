/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import entity.Book;
import entity.History;
import entity.Reader;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import session.BookFacade;
import session.HistoryFacade;
import session.ReaderFacade;
import util.EncriptPass;
import util.PageReturner;

/**
 *
 * @author Melnikov
 */
@WebServlet(name = "Library", urlPatterns = {
    "/newBook",
    "/addBook",
    "/newReader",
    "/addReader",
    "/showBooks",
    "/showReader",
    "/library",
    "/takeBook",
    "/showTakeBook",
    "/returnBook",
    "/deleteBook",
    
})
public class Library extends HttpServlet {
    
@EJB BookFacade bookFacade;
@EJB ReaderFacade readerFacade;
@EJB HistoryFacade historyFacade;
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF8");
        String path = request.getServletPath();
        if(null != path)
            switch (path) {
        case "/newBook":
            request.getRequestDispatcher(PageReturner.getPage("newBook")).forward(request, response);
            break;
        case "/addBook":{
            String nameBook = request.getParameter("nameBook");
            String author = request.getParameter("author");
            String yearPublished = request.getParameter("yearPublished");
            String isbn = request.getParameter("isbn");
            String countStr = request.getParameter("count");
            Book book = new Book(nameBook, author, new Integer(yearPublished), isbn, new Integer(countStr));
            bookFacade.create(book);
            request.setAttribute("book", book);
            request.getRequestDispatcher(PageReturner.getPage("welcome")).forward(request, response);
                break;
            }
        case "/newReader":
            request.getRequestDispatcher(PageReturner.getPage("newReader")).forward(request, response);
            break;
        case "/addReader":{
            String name = request.getParameter("name");
            String surname = request.getParameter("surname");
            String phone = request.getParameter("phone");
            String city = request.getParameter("city");
            String login = request.getParameter("login");
            String password1 = request.getParameter("password1");
            String password2 = request.getParameter("password2");
            if(!password1.equals(password2)){
              request.setAttribute("info", "Неправильно введен логин или пароль");  
              request.getRequestDispatcher(PageReturner.getPage("welcome"))
                      .forward(request, response);
              break;
            }
            EncriptPass ep = new EncriptPass();
            String salts = ep.createSalts();
            String encriptPass = ep.setEncriptPass(password1, salts);
            Reader reader = new Reader(name, surname, phone, city, login, 
                    encriptPass,salts);
            readerFacade.create(reader);
            request.setAttribute("reader", reader);
            request.getRequestDispatcher(PageReturner.getPage("welcome"))
                    .forward(request, response);
                break;
            }
        case "/showBooks":{
            List<Book> listBooks = bookFacade.findActived(true);
            request.setAttribute("listBooks", listBooks);
            request.getRequestDispatcher(PageReturner.getPage("listBook")).forward(request, response);
                break;
            }
        case "/showReader":
            List<Reader> listReader = readerFacade.findAll();
            request.setAttribute("listReader", listReader);
            request.getRequestDispatcher(PageReturner.getPage("listReader")).forward(request, response);
            break;
        case "/library":
            request.setAttribute("listBooks", bookFacade.findActived(true));
            request.setAttribute("listReader", readerFacade.findAll());
            request.getRequestDispatcher(PageReturner.getPage("takeBook")).forward(request, response);
            break;
        case "/showTakeBook":{
            List<History> takeBooks = historyFacade.findTakeBooks();
            request.setAttribute("takeBooks", takeBooks);
            request.getRequestDispatcher(PageReturner.getPage("listTakeBook")).forward(request, response);
                break;
            }
        case "/takeBook":{
            String selectedBook = request.getParameter("selectedBook");
            String selectedReader = request.getParameter("selectedReader");
            Book book = bookFacade.find(new Long(selectedBook));
            
            Reader reader = readerFacade.find(new Long(selectedReader));
            Calendar c = new GregorianCalendar();
            if(book.getCount()>0){
                book.setCount(book.getCount()-1);
                bookFacade.edit(book);
                History history = new History(book, reader, c.getTime(), null);
                historyFacade.create(history);
            }else{
                request.setAttribute("info", "Все книги выданы");
            }
            List<History> takeBooks = historyFacade.findTakeBooks();
            request.setAttribute("takeBooks", takeBooks);
            request.getRequestDispatcher(PageReturner.getPage("listTakeBook")).forward(request, response);
                break;
            }
        case "/returnBook":{
            String historyId = request.getParameter("historyId");
            History history = historyFacade.find(new Long(historyId));
            Calendar c = new GregorianCalendar();
            history.setDateReturn(c.getTime());
            history.getBook().setCount(history.getBook().getCount()+1);
            historyFacade.edit(history);
            List<History> takeBooks = historyFacade.findTakeBooks();
            request.setAttribute("takeBooks", takeBooks);
            request.getRequestDispatcher(PageReturner.getPage("listTakeBook")).forward(request, response);
                break;
            }
        case "/deleteBook":{
            String deleteBookId = request.getParameter("deleteBookId");
            Book book = bookFacade.find(new Long(deleteBookId));
            book.setActive(Boolean.FALSE);
            bookFacade.edit(book);
            //historyFacade.remove(deleteBookId);
            List<Book> listBooks = bookFacade.findActived(true);
            request.setAttribute("listBooks", listBooks);
            request.getRequestDispatcher(PageReturner.getPage("listBook")).forward(request, response);
                break;
            }
        default:
            request.getRequestDispatcher(PageReturner.getPage("welcome")).forward(request, response);
            break;
    }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}