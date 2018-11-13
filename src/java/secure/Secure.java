/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package secure;

import entity.Reader;
import java.io.IOException;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import session.ReaderFacade;
import session.RoleFacade;
import session.UserRolesFacade;
import util.PageReturner;

/**
 *
 * @author Melnikov
 */
@WebServlet(name = "Secure", urlPatterns = {
    "/login",
    "/showLogin",
    "/newRole",
    "/addRole",
    "/editUserRoles",
    "/changeUserRole"
})
public class Secure extends HttpServlet {
   
    @EJB RoleFacade roleFacade;
    @EJB ReaderFacade readerFacade;
    @EJB UserRolesFacade userRolesFacade;
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF8");
        String path = request.getServletPath();
        if(null != path)
            switch (path) {
        case "/login":
            String login = request.getParameter("login");
            String password = request.getParameter("password");
            request.setAttribute("info", "Нет такого пользователя!");
            Reader regUser = readerFacade.fineByLogin(login);
            if(regUser == null){
                request.getRequestDispatcher(PageReturner.getPage("showLogin")).forward(request, response);
                break;
            }
            if(password.equals(regUser.getPassword())){
                HttpSession session = request.getSession(true);
                session.setAttribute("regUser", regUser);
                request.setAttribute("info", "Привет "+regUser.getName()+"! Вы вошли в систему.");
                request.getRequestDispatcher(PageReturner.getPage("welcome")).forward(request, response);
                break;
            }
            request.getRequestDispatcher(PageReturner.getPage("showLogin")).forward(request, response);
            break;
        case "/showLogin":
            request.getRequestDispatcher(PageReturner.getPage("showLogin")).forward(request, response);
            break;
        case "/newRole":
            request.getRequestDispatcher(PageReturner.getPage("newRole")).forward(request, response);
            break;
        case "/addRole":
            String nameRole = request.getParameter("nameRole");
            Role role = new Role();
            role.setName(nameRole.toUpperCase());
            try {
                if(!role.getName().isEmpty()){
                   roleFacade.create(role); 
                }
            } catch (Exception e) {
               request.setAttribute("info", "Такая роль уже существует");
            }
            request.getRequestDispatcher(PageReturner.getPage("newRole")).forward(request, response);
            break;
            
        case "/editUserRoles":
            List<Reader> listUsers = readerFacade.findAll();
            List<Role> listRoles = roleFacade.findAll();
            request.setAttribute("listUsers", listUsers);
            request.setAttribute("listRoles", listRoles);
            request.getRequestDispatcher(PageReturner.getPage("editUserRoles")).forward(request, response);
            break;
        case "/changeUserRole":
            String setButton = request.getParameter("setButton");
            String deleteButton = request.getParameter("deleteButton");
            String userId = request.getParameter("user");
            String roleId = request.getParameter("role");
            Reader reader = readerFacade.find(new Long(userId));
            Role roleToUser = roleFacade.find(new Long(roleId));
            UserRoles ur = new UserRoles(reader, roleToUser);
            
            SecureLogic sl = new SecureLogic();
            if(setButton != null){
                sl.addRoleToUser(ur);
            }
            if(deleteButton != null){
                sl.deleteRoleToUser(ur.getReader());
            }
                
            List<Reader> newListUsers = readerFacade.findAll();
            List<Role> newListRoles = roleFacade.findAll();
            request.setAttribute("listUsers", newListUsers);
            request.setAttribute("listRoles", newListRoles);
            request.getRequestDispatcher(PageReturner.getPage("editUserRoles")).forward(request, response);
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