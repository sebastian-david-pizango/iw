const path = require('path');
const express = require('express');
const store = require('./data-store');

const app = express();
const PORT = process.env.PORT || 8088;

app.set('view engine', 'ejs');
app.set('views', path.join(__dirname, 'paginas'));

app.use(express.urlencoded({ extended: true })); 
app.use(express.static(path.join(__dirname, 'diseño'))); 


function formatFecha(iso) {
    if (!iso) return '';
    const [y, m, d] = iso.split('-');
    if (!y || !m || !d) return iso;
    return `${d}-${m}-${y}`;
}


app.get('/', (req, res) => {
    const { q = '', sort = '', dir = '' } = req.query;
    const universidades = store.search({ q, sort, dir });

    res.render('index', {
        universidades,
        formatFecha,
        q,
        sort,
        dir,
        mensaje: req.query.mensaje || null
    });
});


app.get('/nueva', (req, res) => {
    res.render('form', { modo: 'crear', universidad: null });
});


app.post('/universidades', (req, res) => {
    const { nombre, fecha, resolucion, departamento, provincia, tipoGestion } = req.body;

    if (!nombre || !fecha || !resolucion || !departamento || !provincia || !tipoGestion) {
        return res.status(400).render('form', {
            modo: 'crear',
            universidad: req.body,
            error: 'Todos los campos son obligatorios.'
        });
    }

    store.create({ nombre, fecha, resolucion, departamento, provincia, tipoGestion });
    res.redirect('/?mensaje=Universidad agregada correctamente');
});

app.get('/editar/:id', (req, res) => {
    const universidad = store.getById(req.params.id);
    if (!universidad) {
        return res.redirect('/?mensaje=Registro no encontrado');
    }
    res.render('form', { modo: 'editar', universidad });
});

app.post('/editar/:id', (req, res) => {
    const { nombre, fecha, resolucion, departamento, provincia, tipoGestion } = req.body;

    if (!nombre || !fecha || !resolucion || !departamento || !provincia || !tipoGestion) {
        return res.status(400).render('form', {
            modo: 'editar',
            universidad: { id: req.params.id, ...req.body },
            error: 'Todos los campos son obligatorios.'
        });
    }

    const actualizado = store.update(req.params.id, { nombre, fecha, resolucion, departamento, provincia, tipoGestion });
    if (!actualizado) {
        return res.redirect('/?mensaje=Registro no encontrado');
    }
    res.redirect('/?mensaje=Universidad actualizada correctamente');
});


app.post('/eliminar/:id', (req, res) => {
    store.remove(req.params.id);
    res.redirect('/?mensaje=Universidad eliminada correctamente');
});

app.listen(PORT, () => {
    console.log(`Escuchando en: http://localhost:${PORT}`);
});
