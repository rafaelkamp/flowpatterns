package com.rafaelkamp.flowpatterns;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Interface web - recursos estáticos")
class WebInterfaceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("deve servir a página inicial da SPA")
    void deveServirPaginaInicial() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.html"));
    }

    @Test
    @DisplayName("deve servir os recursos estáticos da interface")
    void deveServirRecursosEstaticos() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("FlowPatterns")))
                .andExpect(content().string(containsString("/js/app.js")))
                .andExpect(content().string(containsString("/css/app.css")));

        mockMvc.perform(get("/css/app.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("text/css")))
                .andExpect(content().string(containsString("--primary")));

        mockMvc.perform(get("/js/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("renderRoute")));

        mockMvc.perform(get("/js/api.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/api/v1")));

        mockMvc.perform(get("/favicon.svg"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("image/svg+xml")));
    }
}
