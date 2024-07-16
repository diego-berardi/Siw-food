package it.uniroma3.siw.controller;

import static it.uniroma3.siw.model.Credentials.CUOCO_ROLE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Cuoco;
import it.uniroma3.siw.model.Ingrediente;
import it.uniroma3.siw.model.Ricetta;
import it.uniroma3.siw.repository.IngredienteRepository;
import it.uniroma3.siw.repository.RicettaRepository;
import it.uniroma3.siw.service.CredentialsService;
import jakarta.validation.Valid;

@Controller
public class IngredienteController {
	
	@Autowired IngredienteRepository ingredienteRepository;
	@Autowired RicettaRepository ricettaRepository;
	@Autowired CredentialsService credentialsService;
	
	
	@GetMapping(value="/formNewIngrediente/{idRicetta}")
	public String formNewIngrediente(@PathVariable Long idRicetta,Model model) {
		model.addAttribute("ingrediente", new Ingrediente());
		model.addAttribute("ricetta", this.ricettaRepository.findById(idRicetta).orElse(null));
		return "formNewIngrediente.html";
	}
	
	@PostMapping("/newIngrediente/{idRicetta}")
	public String newIngrediente(@PathVariable Long idRicetta,@Valid @ModelAttribute Ingrediente ingrediente, BindingResult bindingResult, Model model) {
		Ricetta r=ricettaRepository.findById(idRicetta).orElse(null);
		boolean ingridientAlredyExist=false;
		for(Ingrediente i:r.getIngredienti()) {
			if(i.equals(ingrediente)) {
				ingridientAlredyExist=true;
			}
		}
		if(!ingridientAlredyExist) {
			r.getIngredienti().add(ingrediente);
			this.ingredienteRepository.save(ingrediente);
			this.ricettaRepository.save(r);
		}
			return "redirect:/ricetta/"+r.getId();
	}
	

	@GetMapping("/rimuoviIngrediente/{idRicetta}/{idIngrediente}")
	public String rimuoviIngrediente(@PathVariable Long idRicetta,@PathVariable Long idIngrediente,Model model) {
		Ingrediente i=ingredienteRepository.findById(idIngrediente).orElse(null);
		Ricetta r=ricettaRepository.findById(idRicetta).orElse(null);
		int lunghezza=0;
		for(Ricetta ric: i.getRicette()) {
			lunghezza++;
		}
		r.getIngredienti().remove(i);
		if(i.getRicette()!=null) {
			if(lunghezza==1 && i.getRicette().get(0).equals(r)) {
				ingredienteRepository.delete(i);
			}
		}		
		ricettaRepository.save(r);
		return "redirect:/ricetta/"+r.getId();
	}
}
