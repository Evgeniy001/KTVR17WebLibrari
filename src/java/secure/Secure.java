/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package secure;

import entity.Reader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    "/showLogin",
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

    @Override
    public void init() throws ServletException {
        
        List<Role> roles = roleFacade.findAll();
        if(roles.isEmpty()){
            Role roleAdmin = new Role();
            roleAdmin.setName("ADMIN");
            roleFacade.create(roleAdmin);
            Reader admin = new Reader("admin", "admin", "4545454", "Йыхви", "admin", "admin");
            readerFacade.create(admin);
            UserRoles ur = new UserRoles(admin, roleAdmin);
            userRolesFacade.create(ur);
        }
    }
    
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
        HttpSession session = request.getSession(false);
        Reader regUser=null;
        if(session != null){
            regUser=(Reader) session.getAttribute("user");
        }
        
        SecureLogic sl = new SecureLogic();
        if(null != path)
            switch (path) {
        case "/showLogin":
                request.getRequestDispatcher(PageReturner.getPage("showLogin")).forward(request, response);
            break;
        case "/login":
            String login = request.getParameter("login");
            String password = request.getParameter("password");
            Reader user = readerFacade.findByLogin(login);
            request.setAttribute("info", "Нет такого пользователя");
            if(user != null)
                if(password.equals(user.getPassword())){
                    session = request.getSession(true);
                    session.setAttribute("user", user);
                    request.setAttribute("info", "Вы увспешно вошли в систему!");
                }
            request.getRequestDispatcher(PageReturner.getPage("welcome")).forward(request, response);
            break;
        case "/newRole":
            if(!sl.isRole(regUser, "ADMIN")){
                request.getRequestDispatcher(PageReturner.getPage("showLogin")).forward(request, response);
                break;
            }
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
            if(!sl.isRole(regUser, "ADMIN")){
                request.getRequestDispatcher(PageReturner.getPage("showLogin")).forward(request, response);
                break;
            }
            List<Reader> listUsers = readerFacade.findAll();
            Map<Reader,Role>mapUsers=new HashMap<>();
            int n = listUsers.size();
            for(int i=0;i<n;i++){
                mapUsers.put(listUsers.get(i), sl.getRole(listUsers.get(i)));
            }
            request.setAttribute("mapUsers", mapUsers);
            List<Role> listRoles = roleFacade.findAll();
            request.setAttribute("listRoles", listRoles);
            request.getRequestDispatcher(PageReturner.getPage("editUserRoles")).forward(request, response);
            break;
        case "/changeUserRole":
            if(!sl.isRole(regUser, "ADMIN")){
                request.getRequestDispatcher(PageReturner.getPage("showLogin")).forward(request, response);
                break;
            }
            String userId = request.getParameter("user");
            String roleId = request.getParameter("role");
            if(request.getParameter("setButton") != null){
                Reader reader = readerFacade.find(new Long(userId));
                Role roleToUser = roleFacade.find(new Long(roleId));
                UserRoles ur = new UserRoles(reader, roleToUser);
                sl.addRoleToUser(ur);
            }else if(request.getParameter("deleteButton") != null){
                Reader reader = readerFacade.find(new Long(userId));
                Role roleToUser = roleFacade.find(new Long(roleId));
                UserRoles ur = new UserRoles(reader, roleToUser);
                sl.deleteRoleToUser(ur);
            }
            List<Reader> newListUsers = readerFacade.findAll();
            Map<Reader,Role>newMapUsers=new HashMap<>();
            int n1 = newListUsers.size();
            for(int i=0;i<n1;i++){
                newMapUsers.put(newListUsers.get(i), sl.getRole(newListUsers.get(i)));
            }
            request.setAttribute("mapUsers", newMapUsers);
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