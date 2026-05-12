const express = require('express');
const { GoogleGenerativeAI } = require('@google/generative-ai');
const cors = require('cors');
require('dotenv').config();

const app = express();
app.use(express.json());
app.use(cors());

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);

app.post('/chat', async (req, res) => {
    const { message, context } = req.body;

    if (!message) {
        return res.status(400).send({ error: 'Message is required' });
    }

    try {
        const model = genAI.getGenerativeModel({ 
            model: "gemini-1.5-flash",
            systemInstruction: "You are Corti, a supportive AI for CortiSense. Concise, friendly, and specialized in stress management."
        });

        const prompt = `${context}\n\nUser: ${message}`;
        const result = await model.generateContent(prompt);
        const response = await result.response;
        const text = response.text();

        res.send({ reply: text });
    } catch (error) {
        console.error('AI Error:', error);
        res.status(500).send({ error: 'AI failed to respond' });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`AI Proxy running on port ${PORT}`);
});
