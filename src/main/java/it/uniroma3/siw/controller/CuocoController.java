package it.uniroma3.siw.controller;
import static it.uniroma3.siw.model.Credentials.CUOCO_ROLE;

import java.util.List;
import java.util.Optional;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Cuoco;
import it.uniroma3.siw.model.Image;
import it.uniroma3.siw.model.Ricetta;
import it.uniroma3.siw.repository.CredentialsRepository;
import it.uniroma3.siw.repository.CuocoRepository;
import it.uniroma3.siw.repository.ImageRepository;
import it.uniroma3.siw.repository.RicettaRepository;
import it.uniroma3.siw.repository.UserRepository;
import it.uniroma3.siw.service.CredentialsService;
import jakarta.validation.Valid;
import static it.uniroma3.siw.model.Credentials.ADMIN_ROLE;


@Controller
public class CuocoController {
	@Autowired private CuocoRepository cuocoRepository;
	@Autowired private CredentialsService credentialsService;
	@Autowired private UserRepository userRepository;
	@Autowired private CredentialsRepository credentialsRepository;
	@Autowired private RicettaRepository ricettaRepository;
	@Autowired private ImageRepository imageRepository;


	@GetMapping("/cuochi")
	public String getCuochi(Model model){
				UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		if(credentials.getCuoco()!=null) {
			model.addAttribute("cuoco", credentials.getCuoco());
		}else {
			if(credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
				model.addAttribute("admin",credentials.getRole());
			}else {
				model.addAttribute("user",credentials.getRole());
			}
		}
		model.addAttribute("cuochi", this.cuocoRepository.findAll());
		return "cuochi.html";
	}
	

	@GetMapping("/cuoco/{id}")
	public String getMovie(@PathVariable Long id, Model model) {
		UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		if(credentials.getCuoco()!=null) {
			model.addAttribute("currentCuoco", credentials.getCuoco());
		}else {
			if(credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
				model.addAttribute("admin",credentials.getRole());
			}else {
				model.addAttribute("user",credentials.getRole());
			}
		}
		model.addAttribute("cuoco", this.cuocoRepository.findById(id).get());
		if(credentials.getRole().equals(Credentials.CUOCO_ROLE)) {
			model.addAttribute("cuocoRole", credentials.getRole());
			return "cuoco.html";
		}else {
			if(credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
				model.addAttribute("adminRole", credentials.getRole());
				return "cuocoEditable.html";
			}
		}
		model.addAttribute("userRole", credentials.getRole());
		return "cuoco.html";
	}	

	@GetMapping(value = "/profilePage") 
	public String getProfilePage(Model model) {
		UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		if (credentials.getRole().equals(Credentials.CUOCO_ROLE)) {
			// model.addAttribute("professor", credentials.getRole());
			Cuoco p= this.cuocoRepository.findById(credentials.getCuoco().getId()).get();
			model.addAttribute("ricette", p.getRicette());
			model.addAttribute("cuoco", this.cuocoRepository.findById(credentials.getCuoco().getId()).get());
			return "cuocoProfilePage.html";
		}else if(credentials.getRole().equals(Credentials.DEFAULT_ROLE)){
			model.addAttribute("user", credentials.getRole());
			model.addAttribute("user", this.userRepository.findById(credentials.getUser().getId()).get());
			return "userProfilePage.html";
		}else {
			model.addAttribute("admin", credentials.getRole());
			model.addAttribute("user", this.userRepository.findById(credentials.getUser().getId()).get());
			return "adminProfilePage.html";
		}
	}
	
	
	@GetMapping("/admin/eliminaCuoco/{idCuoco}")
	public String cancellaCuoco(@PathVariable("idCuoco") Long idCuoco, Model model) {
		UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		Cuoco cuoco = this.cuocoRepository.findById(idCuoco).get();
		List<Ricetta> ricette = cuoco.getRicette();
		for(Ricetta r: ricette) {
			r.setCuoco(null);
		}
	    this.cuocoRepository.save(cuoco);
		Iterable<Credentials> allCredentials = this.credentialsRepository.findAll();
		for(Credentials i: allCredentials) {
			if(i.getCuoco() != null) {
				if(i.getCuoco().getId() == idCuoco) {
					if(!i.getRole().equals(Credentials.ADMIN_ROLE)) {
		                i.setCuoco(null);
		                this.credentialsRepository.delete(i);
		            }
				}
			}
		}
		this.cuocoRepository.delete(cuoco);
		model.addAttribute("user", this.userRepository.findById(credentials.getUser().getId()).get());
		return "adminProfilePage.html";
	}
	

	@GetMapping(value="/admin/formNewCuoco")
	public String formNewCuoco(Model model) {
		model.addAttribute("cuoco", new Cuoco());
		return "admin/formNewCuoco.html";
	}

	@PostMapping(value={"/admin/aggiungiCuoco"},consumes = "multipart/form-data")
	public String newCuoco(@Valid @ModelAttribute Cuoco cuoco,@RequestPart("file") MultipartFile file, BindingResult bindingResult, Model model) {
		//		this.movieValidator.validate(movie, bindingResult);
		if (!bindingResult.hasErrors()) {
			try {
				Image i=new Image();
				i.setImageData(file.getBytes());
				cuoco.setProfileImage(i);
				this.imageRepository.save(i);
			} catch (Exception e) {
				System.out.println("erroreeee");
			}
			this.cuocoRepository.save(cuoco);
			return "redirect:/cuoco/"+cuoco.getId();
		} else {
			return "admin/formNewCuoco.html"; 
		}
	}
}
