package it.uniroma3.siw.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Cuoco;
import it.uniroma3.siw.model.Image;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.repository.ImageRepository;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.CuocoService;
import it.uniroma3.siw.service.UserService;
import jakarta.validation.Valid;


@Controller
public class AuthenticationController {
	@Autowired
	private CredentialsService credentialsService;
	@Autowired private UserService userService;
	@Autowired private CuocoService cuocoService;
	@Autowired private ImageRepository imageRepository;
	

	@GetMapping(value = "/") 
	public String index(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof AnonymousAuthenticationToken) {
	        return "index.html";
		}
		else {		
			UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
			if (credentials.getCuoco()!=null) {
				model.addAttribute("cuoco", credentials.getCuoco());
				return "index.html";
			}else {
				if(credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
					model.addAttribute("admin", credentials.getUser());
				}else {
					model.addAttribute("user", credentials.getUser());
				}
			}
		}
        return "index.html";
	}


	@GetMapping(value = "/login") 
	public String showLoginForm (Model model) {
		return "loginPage.html";
	}
	

	@GetMapping(value = "/register") 
	public String showRegisterForm (Model model) {
		model.addAttribute("user", new User());
		model.addAttribute("credentials", new Credentials());
		return "registerUser.html";
	}
	
	@PostMapping(value = { "/register" })
    public String registerUser(@Valid @ModelAttribute User user,
                 BindingResult userBindingResult, @Valid
                 @ModelAttribute Credentials credentials,
                 BindingResult credentialsBindingResult,
                 Model model) {

		// se user e credential hanno entrambi contenuti validi, memorizza User e the Credentials nel DB
        if(!userBindingResult.hasErrors() && !credentialsBindingResult.hasErrors()) {
            userService.saveUser(user);
            credentials.setUser(user);
            credentialsService.saveCredentials(credentials);
            model.addAttribute("user", user);
            return "logInPage.html";
        }
        return "registerUser.html";
    }
	
	/*GET DELLA PAGINA PER REGISTRARE I DATI E POST PER INSERIRE I DATI NEL DB DI UN PROFESSORE*/
	@GetMapping(value = {"/registerCuoco"}) 
	public String showRegisterFormCuoco (Model model) {
		model.addAttribute("cuoco", new Cuoco());
		model.addAttribute("credentials", new Credentials() );
		return "registerCuoco.html";
	}
	
	@PostMapping(value = { "/registerCuoco" },consumes = "multipart/form-data")
    public String registerCuoco(@Valid @ModelAttribute Cuoco cuoco,@RequestPart("file") MultipartFile file,
                 BindingResult userBindingResult, @Valid
                 @ModelAttribute Credentials credentials,
                 BindingResult credentialsBindingResult,
                 Model model) {

		// se user e credential hanno entrambi contenuti validi, memorizza User e the Credentials nel DB
        if(!userBindingResult.hasErrors() && !credentialsBindingResult.hasErrors()) {
        	try {
				Image image=new Image();
				image.setImageData(file.getBytes());
				cuoco.setProfileImage(image);
				this.imageRepository.save(image);
			} catch (Exception e) {
				System.out.println("erroreeee");
			}
            cuocoService.saveCuoco(cuoco);
            credentials.setCuoco(cuoco);
            credentialsService.saveCredentials(credentials);
            model.addAttribute("cuoco", cuoco);
            return "logInPage.html";
        }
        return "registerCuoco.html";
    }
	
	/*GET CHE MOSTRA LA HOME PAGE DOPO LOG IN*/
	@GetMapping(value = "/success")
    public String defaultAfterLogin(Model model) {
        
    	UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
    	if(credentials.getRole()!=null) {
	    	if (credentials.getCuoco()!=null) {
	    		model.addAttribute("cuoco", credentials.getCuoco());
	    		return "index.html";
	        }else {
				if(credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
					model.addAttribute("admin",credentials.getRole());
				}else {
					model.addAttribute("user",credentials.getRole());
				}
			}
    	}
        return "index.html";
    }
	
	

	
	
}