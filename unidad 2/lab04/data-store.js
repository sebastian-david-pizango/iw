const fs = require('fs');
const path = require('path');
const { parse } = require('csv-parse/sync');
const { stringify } = require('csv-stringify/sync');

const CSV_PATH = path.join(__dirname, 'data', 'universidades.csv');
const COLUMNS = ['id', 'nombre', 'fecha', 'resolucion', 'departamento', 'provincia', 'tipoGestion'];


function ensureFile() {
    if (!fs.existsSync(CSV_PATH)) {
        fs.mkdirSync(path.dirname(CSV_PATH), { recursive: true });
        fs.writeFileSync(CSV_PATH, COLUMNS.join(',') + '\n', 'utf8');
    }
}


function readAll() {
    ensureFile();
    const raw = fs.readFileSync(CSV_PATH, 'utf8');
    if (!raw.trim()) return [];
    const records = parse(raw, {
        columns: true,
        skip_empty_lines: true,
        trim: true
    });
    return records.map(r => ({
        id: Number(r.id),
        nombre: r.nombre || '',
        fecha: r.fecha || '',
        resolucion: Number(r.resolucion) || 1,
        departamento: r.departamento || '',
        provincia: r.provincia || '',
        tipoGestion: r.tipoGestion || ''
    }));
}

function writeAll(records) {
    ensureFile();
    const output = stringify(records, {
        header: true,
        columns: COLUMNS
    });
    fs.writeFileSync(CSV_PATH, output, 'utf8');
}

function getNextId(records) {
    if (records.length === 0) return 1;
    return Math.max(...records.map(r => r.id)) + 1;
}


function getAll() {
    return readAll();
}

function getById(id) {
    return readAll().find(r => r.id === Number(id));
}

function create(data) {
    const records = readAll();
    const nuevo = {
        id: getNextId(records),
        nombre: data.nombre.trim(),
        fecha: data.fecha,
        resolucion: Number(data.resolucion),
        departamento: data.departamento.trim(),
        provincia: data.provincia.trim(),
        tipoGestion: data.tipoGestion
    };
    records.push(nuevo);
    writeAll(records);
    return nuevo;
}

function update(id, data) {
    const records = readAll();
    const idx = records.findIndex(r => r.id === Number(id));
    if (idx === -1) return null;
    records[idx] = {
        id: Number(id),
        nombre: data.nombre.trim(),
        fecha: data.fecha,
        resolucion: Number(data.resolucion),
        departamento: data.departamento.trim(),
        provincia: data.provincia.trim(),
        tipoGestion: data.tipoGestion
    };
    writeAll(records);
    return records[idx];
}

function remove(id) {
    const records = readAll();
    const filtered = records.filter(r => r.id !== Number(id));
    writeAll(filtered);
    return filtered.length !== records.length;
}

function formatFechaVisible(iso) {
    if (!iso) return '';
    const [y, m, d] = iso.split('-');
    if (!y || !m || !d) return iso;
    return `${d}-${m}-${y}`;
}

function search({ q, sort, dir } = {}) {
    let records = readAll();

    if (q && q.trim()) {
        const term = q.trim().toLowerCase();
        records = records.filter(r =>
            r.nombre.toLowerCase().includes(term) ||
            r.fecha.toLowerCase().includes(term) ||
            formatFechaVisible(r.fecha).toLowerCase().includes(term) ||
            r.departamento.toLowerCase().includes(term)
        );
    }

    if (sort && COLUMNS.includes(sort)) {
        const factor = dir === 'desc' ? -1 : 1;
        records.sort((a, b) => {
            const va = a[sort];
            const vb = b[sort];
            if (typeof va === 'number' && typeof vb === 'number') {
                return (va - vb) * factor;
            }
            return String(va).localeCompare(String(vb), 'es') * factor;
        });
    }

    return records;
}

module.exports = { getAll, getById, create, update, remove, search };
