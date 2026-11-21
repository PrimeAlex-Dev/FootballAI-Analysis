import { useList } from '../shared/useList'
import EntityLogo from '../shared/EntityLogo'
import { competitionApi } from './api'
import CompetitionForm from './CompetitionForm'
import {
  Button,
  Field,
  ListTable,
  PageHeading,
  Pagination,
  RowActions,
} from '../shared/ui'
import { useRecordDialogs } from '../shared/useRecordDialogs'
export default function CompetitionsPage() {
  const list = useList(competitionApi.list)
  const records = useRecordDialogs(competitionApi, list.refresh)
  return (
    <>
      <PageHeading
        title="Giải đấu"
        description="Quản lý thông tin và loại hình giải đấu."
        createLabel="Thêm giải đấu"
        onCreate={records.create}
      />
      {records.notice && (
        <p className="success-notice" role="status">
          {records.notice}
        </p>
      )}
      <section className="ui-card management-card">
        <form
          className="filter-bar"
          onSubmit={(event) => {
            event.preventDefault()
            list.change({
              name: String(
                new FormData(event.currentTarget).get('name') || '',
              ).trim(),
            })
          }}
        >
          <Field label="Tên giải đấu">
            <input
              key={list.params.get('name')}
              defaultValue={list.params.get('name') || ''}
              name="name"
              placeholder="Tìm theo tên…"
            />
          </Field>
          <Field label="Loại">
            <select
              value={list.params.get('type') || ''}
              onChange={(event) => list.change({ type: event.target.value })}
            >
              <option value="">Tất cả loại</option>
              <option value="LEAGUE">League</option>
              <option value="CUP">Cup</option>
            </select>
          </Field>
          <Button type="submit">Tìm kiếm</Button>
          <Button
            secondary
            onClick={() => {
              list.change({ name: '', type: '' })
            }}
          >
            Đặt lại
          </Button>
        </form>
        <ListTable
          headers={['ID', 'Tên giải đấu', 'Loại', 'Thao tác']}
          loading={list.loading}
          error={list.error}
          empty={!list.data?.content.length}
          retry={list.refresh}
        >
          {list.data?.content.map((item) => (
            <tr key={item.id}>
              <td>#{item.id}</td>
              <td>
                <span className="competition-name">
                  <EntityLogo
                    key={item.logo}
                    name={item.name}
                    logo={item.logo}
                  />
                  {item.name}
                </span>
              </td>
              <td>
                <span className="type-badge">{item.type}</span>
              </td>
              <td>
                <RowActions
                  view={() => records.select('view', item)}
                  edit={() => records.select('edit', item)}
                  remove={() => records.select('delete', item)}
                />
              </td>
            </tr>
          ))}
        </ListTable>
        <Pagination
          page={list.page}
          size={list.size}
          total={list.data?.page.totalElements || 0}
          pages={list.data?.page.totalPages || 0}
          disabled={list.loading || !!list.error}
          changeSize={(size) => list.change({ size })}
          go={list.go}
        />
      </section>
      {(records.creating || records.selection?.mode === 'edit') && (
        <CompetitionForm
          value={records.selection?.value}
          onClose={records.close}
          onSuccess={records.success}
        />
      )}
      {records.dialogs(
        (item) => item.name,
        (item) => (
          <>
            <dt>ID</dt>
            <dd>#{item.id}</dd>
            <dt>Tên</dt>
            <dd>{item.name}</dd>
            <dt>Loại</dt>
            <dd>{item.type}</dd>
            <dt>Logo</dt>
            <dd className="break-text">{item.logo || 'Chưa có logo'}</dd>
          </>
        ),
      )}
    </>
  )
}
